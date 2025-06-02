import oracledb
import pandas as pd
import os
import glob
import re

current_dir = os.path.dirname(os.path.abspath(__file__))
print(current_dir)
infile_path = os.path.abspath(os.path.join(current_dir, '..','InFile'))
print(infile_path)


def get_database_details():
    config_path = os.path.abspath(os.path.join(current_dir, '..', '..','..','.reports/config.groovy'))

    with open(config_path, 'r') as f:
        content = f.read()

    dsn_reg = re.search(r'dataSources\.pva\.url\s*=\s*["\']jdbc:oracle:thin:@([^"\']+)["\']', content)
    dsn = dsn_reg.group(1).strip()
    return dsn


def db_connect_oracle():
    try:
        user = input("Enter Oracle username: ")
        password = input("Enter Oracle password: ")
        dsn = get_database_details()

        conn = oracledb.connect(user=user, password=password, dsn=dsn, config_dir="")

        print("Connected to database successfully")
        return conn
    except oracledb.DatabaseError as err:
        print(f"Could not connect to database: {err}")
    return None


def process_csv():
    conn = db_connect_oracle()
    if not conn:
        return  # Exit if connection is not made to the database

    cursor = conn.cursor()
    try:
        table_name = "PVR_IMP_AGG_HISTORY"  #input("Enter table name for which data needs to be loaded: ")
        csv_file_folder = infile_path

        if not os.path.exists(csv_file_folder):
            print(f"The specified folder '{csv_file_folder}' doesn't exist.")
            return

        csv_files = glob.glob(os.path.join(csv_file_folder, "Submitted_Data_*.csv"))

        if not csv_files:
            print(f"No data files found matching the pattern 'Submitted_Data_*.csv' in '{csv_file_folder}'.")
            return

        for file in csv_files:
            print(f"Processing file: {file}")
            df = pd.read_csv(file, keep_default_na=False, na_values=["", "NA", "NaN"])
            df = df.convert_dtypes()  # Automatically infer and convert correct data types
            df = df.where(pd.notnull(df), None)  # Convert NaN to None for proper insertion
            # df["FILENAME"] = os.path.basename(file)  # Track data source

            # Convert int64/float64 to Python's int/float
            for col in df.select_dtypes(include=["Int64", "Float64"]).columns:
                df[col] = df[col].astype(object).where(df[col].notna(), None)

            cols = ", ".join(f'"{col}"' for col in df.columns)
            holder = ", ".join([f":{i + 1}" for i in range(len(df.columns))])  # Bind variables for SQL
            sql_query = f"INSERT INTO {table_name} ({cols}) VALUES ({holder})"

            data = [tuple(row) for row in df.itertuples(index=False)]
            cursor.executemany(sql_query, data)
            conn.commit()

        print("Data inserted into table successfully.")
    except Exception as e:
        print(f"Error processing CSV files: {e}")
    finally:
        cursor.close()
        conn.close()
        print("Database connection closed.")


if __name__ == "__main__":
    process_csv()
