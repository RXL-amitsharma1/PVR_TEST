package com.rxlogix.jasperserver
import com.fasterxml.jackson.databind.ObjectMapper

import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

public class Query extends Resource {
    private static final ObjectMapper mapper = new ObjectMapper();

    private static final int MAX_QUERY_TEXT = 3600;
    private Resource dataSource = null;
    private String language;
    private String sql;
    private String parameters;

    public Resource getDataSource() {
        return dataSource;
    }

    public void setDataSource(Resource dataSource) {
        this.dataSource = dataSource;
    }

    public String getSql() {
        return sql;
    }


    public void setSql(String sql) {
        this.sql = sql;
    }

    protected Class getClientItf() {
        return Query.class;
    }

/*
    protected void copyFrom(Resource clientRes, ReferenceResolver referenceResolver) {
        super.copyFrom(clientRes, referenceResolver);

        Query query = (Query) clientRes;
        copyDataSource(referenceResolver, query);
        setLanguage(query.getLanguage());
        String queryText = query.getSql();
        if (queryText.length() > MAX_QUERY_TEXT) {
            byte[] compressed;
            try {
                compressed = compress(queryText);
            } catch (IOException e) {
                throw new RuntimeException("unexpected i/o exception on compression", e);
            }
            queryText = new BASE64Encoder().encode(compressed);
        }
        setSql(queryText);

        try {
            if (query.getParameters() != null) {
                setParameters(mapper.writeValueAsString(new ParametersContainer(query.getParameters())));
            }
        } catch (IOException e) {
            throw new JSException(e.getMessage());
        }

    }

    protected void copyTo(Resource clientRes, ResourceFactory resourceFactory) {
        super.copyTo(clientRes, resourceFactory);

        Query query = (Query) clientRes;
        query.setDataSource(getClientReference(getDataSource(), resourceFactory));
        query.setLanguage(getLanguage());
        String queryText = getSql();
        // try uncompressing, in case it's compressed
        // if it's not it will fail silently
        try {
            // normalize line breaks
            String normalizedBuffer = queryText.replaceAll("\\s+", System.getProperty("line.separator"));
            byte[] gzipped = new BASE64Decoder().decodeBuffer(normalizedBuffer);
            queryText = uncompress(gzipped);
        } catch (Exception e) {
        }
        query.setSql(queryText);

        if (parameters != null) {
            try {
                query.setParameters(mapper.readValue(parameters, ParametersContainer.class).getParams());
            } catch (IOException e) {
                throw new JSException(e.getMessage());
            }
        }
    }
*/

    public static String uncompress(byte[] compressed) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
        GZIPInputStream gzis = new GZIPInputStream(bais);
        BufferedReader br = new BufferedReader(new InputStreamReader(gzis));
        String line;
        StringBuffer sb = new StringBuffer();
        while ((line = br.readLine()) != null) {
            sb.append(line);
            sb.append("\n");
        }
        return sb.toString();
    }

    public static byte[] compress(String string) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream gzos = new GZIPOutputStream(baos);
        OutputStreamWriter osw = new OutputStreamWriter(gzos);
        osw.write(string);
        osw.flush();
        osw.close();
        return baos.toByteArray();
    }
/*
    private void copyDataSource(ReferenceResolver referenceResolver, Query query) {
        ResourceReference ds = query.getDataSource();
        if (ds != null) {
            Resource repoDS = getReference(ds, RepoReportDataSource.class, referenceResolver);
            if (repoDS != null && !(repoDS instanceof RepoReportDataSource)) {
                throw new JSException("jsexception.query.datasource.has.an.invalid.type", new Object[] {
                    repoDS.getClass().getName()
                });
            }
            setDataSource(repoDS);
        } else {
            setDataSource(null);
        }
    }
*/
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }
/*
    private static class ParametersContainer {
        private List<QueryParameterDescriptor> params;

        ParametersContainer() {}

        ParametersContainer(List<QueryParameterDescriptor> params) {
            this.params = params;
        }

        public List<QueryParameterDescriptor> getParams() {
            return params;
        }

        public void setParams(List<QueryParameterDescriptor> params) {
            this.params = params;
        }
    }
*/
}
