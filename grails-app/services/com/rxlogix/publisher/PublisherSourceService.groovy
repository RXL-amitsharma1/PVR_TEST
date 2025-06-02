package com.rxlogix.publisher


import com.rxlogix.config.BasicPublisherSource
import grails.converters.JSON
import grails.core.GrailsApplication
import grails.util.Holders

import java.nio.file.Files

class PublisherSourceService {

    GrailsApplication grailsApplication
    def oneDriveRestService

    byte[] getData(BasicPublisherSource source) {
        getDataMap(source).data
    }

    Map getDataMap(BasicPublisherSource source) {
        Map result = [data: [], contntType: "", name: ""]
        if (source.fileSource in [BasicPublisherSource.Source.HTTPS, BasicPublisherSource.Source.HTTP]) {
            String path = (source.fileSource == BasicPublisherSource.Source.HTTPS ? "https://" : "http://") + source.path.trim()
            URL url = new URL(path);
            URLConnection connection = url.openConnection();
            connection.connect();
            String contentType = connection.getContentType();
            result.contentType = contentType
            String fieldValue = connection.getHeaderField("Content-Disposition");
            if (fieldValue == null || !fieldValue.contains("filename=\"")) {
                result.name = source.name + toExt(source)
            } else {
                result.name = fieldValue.substring(fieldValue.indexOf("filename=\"") + 10, fieldValue.length() - 1);
            }
            checkContentType(contentType, source.fileType)
            result.data = connection.getInputStream().bytes

        } else if (source.fileSource == BasicPublisherSource.Source.FILE) {
            result.contentType = source.ext
            result.data = source.data
            result.name = source.path
        } else if (source.fileSource == BasicPublisherSource.Source.FOLDER) {
            File f = new File(source.path)
            String contentType = Files.probeContentType(f.toPath())
            checkContentType(contentType, source.fileType)
            result.contentType = contentType
            result.data = f.getBytes()
            result.name = f.getName()
        } else if (source.fileSource == BasicPublisherSource.Source.ONEDRIVE) {
            result = oneDriveRestService.getFile(source.oneDriveSiteId, source.oneDriveFolderId, source.oneDriveUserSettings)
            if (result.error) {
                if (result.exception)
                    throw new Exception("Unable to fetch file ${source.name} !", result.exception)
                else
                    throw new Exception("Unable to fetch file ${source.name} ! OneDrive response: " + result.errorMessage)
            }
        } else if (source.fileSource == BasicPublisherSource.Source.SERVICE) {
            result = runScript(source)
        }
        if (!result.data) throw new Exception("Unable to fetch file ${source.name} !")
        result
    }

    Map runScript(BasicPublisherSource source) {
        StringBuilder log = new StringBuilder()
        try {
            List<String> commands = source.script.split("\n")?.collect { it.trim() }
            Map<String, String> variables = [:]
            Command cmd
            for (int i = 0; i < commands.size(); i++) {
                String row = commands[i]
                if (row.startsWith("GET") || row.startsWith("POST")) {
                    if (cmd) {
                        cmd.execute(variables, log)
                    }
                    cmd = new Command()
                    cmd.source = source
                    String[] part = row.split(" ")
                    cmd.method = part[0].trim()
                    cmd.url = part[1].trim()
                }

                if (row.startsWith("var")) {
                    String[] part = row.substring(3).trim().split("=")
                    String value = part[1].trim()
                    if (cmd && value.startsWith("response.")) {
                        cmd.execute(variables, log)
                        String path = value.substring(9).trim()
                        if (path)
                            variables.put(part[0].trim(), getNode(cmd.response, path))
                        else
                            variables.put(part[0].trim(), cmd.response.toString())
                        cmd = null
                    } else if (value.startsWith("eval ")) {
                        variables.put(part[0].trim(), Eval.x(variables, value.substring(4))?.toString())
                    } else {
                        variables.put(part[0].trim(), value.trim())
                    }
                }
                if (row.startsWith("header")) {
                    String[] part = row.substring(6).trim().split(":")
                    cmd.headers.put(part[0].trim(), part[1].trim())
                }
                if (row.startsWith("body")) {
                    cmd.body = row.substring(4)
                    i++
                    while ((i < commands.size()) && (commands[i]) && !commands[i].startsWith("GET ") && !commands[i].startsWith("POST ") && !commands[i].startsWith("header ") && !commands[i].startsWith("var ")) {
                        cmd.body += commands[i]
                        i++
                    }
                    i--
                }
            }
            if (cmd) {
                cmd.execute(variables, log)
                return cmd.result
            } else {
                return [log: "Wrong service description!"]
            }
        } catch (Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String message = errors.toString()
            log.append(message)
            return [code: 500, log: log, message: message]
        }
    }

    static Object getNode(obj, String path) {
        def cur = obj
        path.split("\\.").each {
            cur = cur."${it}"
        }
        cur.toString()
    }


    String toExt(BasicPublisherSource source) {
        if (source.fileType == BasicPublisherSource.FileType.WORD) return ".docx"
        if (source.fileType == BasicPublisherSource.FileType.EXCEL) return ".xlsx"
        if (source.fileType == BasicPublisherSource.FileType.PDF) return ".pdf"
        if (source.fileType == BasicPublisherSource.FileType.IMAGE) return ".png"
        if (source.fileType == BasicPublisherSource.FileType.XML) return ".xml"
        if (source.fileType == BasicPublisherSource.FileType.JSON) return ".json"
    }

    static void checkContentType(String contentType, BasicPublisherSource.FileType fileType) {

        if (("application/octet-stream"!=contentType)&&
                ((fileType == BasicPublisherSource.FileType.WORD) && (contentType != 'application/vnd.openxmlformats-officedocument.wordprocessingml.document')) ||
                ((fileType == BasicPublisherSource.FileType.EXCEL) && (contentType != 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet')) ||
                ((fileType == BasicPublisherSource.FileType.PDF) && (contentType != 'application/pdf')) ||
                ((fileType == BasicPublisherSource.FileType.XML) && (contentType != 'application/xml')) ||
                ((fileType == BasicPublisherSource.FileType.JSON) && !(contentType in ['application/json', 'text/json'])) ||
                ((fileType == BasicPublisherSource.FileType.IMAGE) && (!contentType.startsWith("image")))
        ) throw new PublisherContentTypeException("Downloaded file has content type: ${contentType} while expecting type is ${fileType}")
    }

    static class Command {
        BasicPublisherSource source
        String url
        String method
        Map<String, String> headers = [:]
        String body
        Map result = [:]
        Object response

        String toString() {
            return "URL: ${url}\nMethod:${method}\nHeaders:${headers?.collect { it.key + ":" + it.value }?.join("\n")}\nBody:${body ?: ""}\n"
        }

        void execute(Map<String, String> variables, StringBuilder log) {
            log.append("\n\nStarting execution for service: " + toString())
            String u = replaceVariables(variables, this.url)
            log.append("Set url: ${u}\n")
            URL ref = new URL(u)
            URLConnection connection = ref.openConnection()
            connection.with {
                doOutput = true
                requestMethod = method
                headers?.each { String name, String value ->
                    String v = replaceVariables(variables, value)
                    if (v.startsWith("Basic") && (v.indexOf(" ") > 0))
                        v = createBasic(v)
                    setRequestProperty(name, v)
                    log.append("Set header: ${name}:${v}\n")
                }
                if (body) {
                    String b = replaceVariables(variables, body)
                    log.append("Set body: ${b}\n")
                    outputStream.write(b.getBytes("UTF-8"))
                }
                connect()
                log.append("Executing...\n")
                result.code = responseCode
                log.append("responseCode: ${responseCode}\n")
                if (result.code != 200) {
                    throw new Exception("Error occurred while fetching data from WebService ${source.getName()} " + getErrorStream().text)
                } else {
                    result.contentType = contentType
                    log.append("contentType: ${contentType}\n")
                    result.data = inputStream.bytes
                }
            }
            checkContentType(result.contentType, source.fileType)
            String fieldValue = connection.getHeaderField("Content-Disposition");
            if (fieldValue && fieldValue.contains("filename=\"")) {
                result.name = fieldValue.substring(fieldValue.indexOf("filename=\"") + 10, fieldValue.length() - 1);
            }
            if (result.contentType in Holders.config.getProperty('grails.mime.types.json')) {
                String out = new String(result.data, "UTF-8")
                log.append("JSON response detected: ${out}\n")
                response = JSON.parse(out)
            } else if (result.contentType in Holders.config.getProperty('grails.mime.types.xml')) {
                String out = new String(result.data, "UTF-8")
                log.append("XML response detected: ${out}\n")
                response = new XmlSlurper().parseText(out)
            } else if (result.contentType.startsWith("text")) {
                String out = new String(result.data, "UTF-8")
                log.append("TEXT response detected: ${out}\n")
                response = out
            }
            result.log = log
            result
        }

        String createBasic(String s) {
            String str = s.substring("Basic ".length()).trim().tokenize(" ").join(":")
            byte[] authEncBytes = Base64.encoder.encode(str.getBytes("UTF-8"));
            String authStringEnc = new String(authEncBytes);
            return "Basic " + authStringEnc
            //return "Basic dXNlcjpwYXNzd2Q="
        }
    }

    static replaceVariables(Map<String, String> variables, String str) {
        String result = str
        variables.each { String name, String value ->
            result = result.replaceAll("\\{\\{" + name + "\\}\\}", value)
        }
        return result
    }

}
