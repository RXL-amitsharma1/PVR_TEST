databaseChangeLog = {

    changeSet(author: "pomipark", id:'replace pipes with commas - query') {
        sql("UPDATE super_query SET query = REPLACE(query, '||', ';');")
    }

    changeSet(author: "pomipark", id:'replace pipes with commas - blank values') {
        sql("UPDATE param SET value = REPLACE(value, '||', ';');")
    }
}
