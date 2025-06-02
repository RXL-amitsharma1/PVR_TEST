def grps = new File("/Users/leigao/Documents/work/rxlogix/pvreports/grails-app/conf/metadata/reportFieldGroup.csv")
def grpsArray =  grps.text.split("\n")

def fields = new File("/Users/leigao/Documents/work/rxlogix/pvreports/grails-app/conf/metadata/argusColumnMasterToReportFieldMapping.csv").text

grpsArray.eachWithIndex {txt, i->
  def j = i + 1
  fields = fields.replace(",$j,", ",$txt,")
}

def tmpFile = new File("/Users/leigao/Documents/work/rxlogix/pvreports/grails-app/conf/test.txt")
tmpFile.write(fields)
