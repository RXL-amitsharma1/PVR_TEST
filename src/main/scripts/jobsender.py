#!/usr/bin/python2.7
#
import requests
import time
import argparse
import sys

# Parse command line options
# parser = argparse.ArgumentParser(description='Submit a job to Spotfire Automation Services')
# parser.add_argument('-f','--jobfile',action='store', dest="jobfile",help='A valid XML job file', required=True)
# parser.add_argument('-u','--url',action='store',dest="url",help='Spotfire server URL', required=True)
# args = parser.parse_args()

url = "http://10.100.6.8:8888"

# URLs
manifest_url = url + "/spotfire/manifest"
status_url = url + "/spotfire/rest/as/job/status/"
start_url = url + "/spotfire/rest/as/job/start"

username = "admin"
password = "admin"
date = time.strftime("%c")
post_headers = {'Content-Type': 'application/xml'}
status_code=""

# Main

# jobdata = open(args.jobfile,'r')
jobdata = """<?xml version="1.0" encoding="utf-8"?>
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

# Start a new session
session = requests.Session()

# Request the server manifest
session.get(manifest_url)

#print '{0} | {1} | {2}'.format(time.strftime("%c"),"n/a","Submitting job to server")

# POST the job data
# request = session.post(start_url, auth=(username,password), data=jobdata, headers=post_headers)

request = session.get("http://10.100.6.8:8888/spotfire")
response = request.text

print(response)

# print "{0} | {1} | {2}".format(time.strftime("%c"),"n/a","Job submitted")
#print "{0} | {1} | {2}".format(time.strftime("%c"),response["StatusCode"],response["Message"])

#status_url = status_url + response["JobId"]

#while status_code != "Finished" and status_code != "Failed":
#    request = session.get(status_url)
#    response = request.json()
#    status_code = response["StatusCode"]
#    #print "{0} | {1} | {2}".format(time.strftime("%c"),response["StatusCode"],response["Message"])
#    time.sleep(1)