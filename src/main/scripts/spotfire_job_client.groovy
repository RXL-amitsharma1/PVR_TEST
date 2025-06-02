import java.util.Base64
import java.nio.charset.StandardCharsets

println "The spot fire client is running"

def server = "10.100.6.8"
def port = 8888
def path = "spotfire/rest/as/job/start"

String username = "admin"
String password = "admin"

def content =
        """
<?xml version="1.0" encoding="utf-8"?>
<as:Job xmlns:as="urn:tibco:spotfire.dxp.automation">
  <as:Tasks>
    <OpenAnalysisFromLibrary xmlns="urn:tibco:spotfire.dxp.automation.tasks">
      <as:Title>Open Analysis from Library</as:Title>
      <AnalysisPath>/Reports/Sales and Marketing</AnalysisPath>
    </OpenAnalysisFromLibrary>
    <SaveAnalysisToLibrary xmlns="urn:tibco:spotfire.dxp.automation.tasks">
      <as:Title>Save Analysis to Library</as:Title>
      <LibraryPath>/Reports/Sales and Marketing47</LibraryPath>
      <EmbedData>false</EmbedData>
      <DeleteExistingBookmarks>false</DeleteExistingBookmarks>
      <AnalysisDescription>{jobid}</AnalysisDescription>
    </SaveAnalysisToLibrary>
  </as:Tasks>
</as:Job>
"""

String authString = username + ":" + password
println("auth string: " + authString)
byte[] authEncBytes = Base64.getEncoder().encode(authString.getBytes())
String authStringEnc = new String(authEncBytes, "UTF-8")
println("Base64 encoded auth string: " + authStringEnc)

URL url = new URL("http://$username:$password@$server:$port/$path")
HttpURLConnection conn = url.openConnection() as HttpURLConnection

byte[] postData = content.getBytes(StandardCharsets.UTF_8)
int postDataLength = postData.length
conn.setDoOutput(true)
conn.setInstanceFollowRedirects(false)
conn.setRequestProperty("Authorization", "Basic " + authStringEnc)
conn.setRequestMethod("POST")
conn.setRequestProperty("Content-Type", "application/xml")
conn.setRequestProperty("charset", "utf-8")
conn.setRequestProperty("Content-Length", Integer.toString(postDataLength))
conn.setUseCaches(false)
try {
    DataOutputStream wr = new DataOutputStream(conn.getOutputStream())
    wr.write(postData)
    InputStream is = conn.getInputStream()
    InputStreamReader isr = new InputStreamReader(is)

    int numCharsRead
    char[] charArray = new char[1024]
    StringBuffer sb = new StringBuffer()
    while ((numCharsRead = isr.read(charArray)) > 0) {
        sb.append(charArray, 0, numCharsRead)
    }
    String result = sb.toString()
    println("*** BEGIN ***")
    println(result)
    println("*** END ***")
} catch (Throwable t) {
    t.printStackTrace()
}

