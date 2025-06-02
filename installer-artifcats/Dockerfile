FROM tomcat:9.0.82-jdk8-corretto-al2
EXPOSE 8080
COPY ./Arial /usr/share/fonts/Arial
COPY ./dejavu /usr/share/fonts/dejavu
COPY ./reports.war /usr/local/tomcat/webapps/reports.war
RUN rm -rf /usr/local/tomcat/conf/server.xml
COPY ./server.xml /usr/local/tomcat/conf/server.xml
ENV JAVA_OPTS="-Xms2G -Xmx8G -XX:+CMSClassUnloadingEnabled -Duser.timezone=GMT -Duser.home=/opt/prod/conf -Dfile.encoding=UTF-8"
CMD ["catalina.sh", "run"]
