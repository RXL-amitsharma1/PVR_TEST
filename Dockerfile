FROM tomcat:9.0-jdk8-corretto
ARG VERSION=unknown
LABEL PV Report - version=$VERSION
RUN yum install shadow-utils vim telnet -y && yum clean all -y && rm -rf /var/cache/yum
# Create user and set ownership and permissions as required
RUN groupadd -g 10001 app_user && \
   useradd -u 10000 -g app_user app_user \
   && chown -R app_user:app_user /usr/local/tomcat
# Copy application files
COPY ./build/libs/reports.war /usr/local/tomcat/webapps/reports.war
# Switch to user - app_user
USER app_user:app_user
# Java Paramentrs
ENV JAVA_OPTS="-Xms2G -Xmx8G -XX:+CMSClassUnloadingEnabled -Duser.timezone=GMT -Duser.home=/opt/prod/conf -Dfile.encoding=UTF-8"
# Open Port
EXPOSE 8080
HEALTHCHECK NONE
CMD ["catalina.sh", "run"]