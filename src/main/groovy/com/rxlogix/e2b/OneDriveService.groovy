package com.rxlogix.e2b

import com.bhyoo.onedrive.client.Client
import com.bhyoo.onedrive.container.items.FolderItem
import com.bhyoo.onedrive.container.items.pointer.PathPointer
import com.bhyoo.onedrive.network.async.UploadFuture
import grails.util.Holders
import groovy.util.logging.Slf4j
import com.rxlogix.Constants

import java.nio.file.Paths


@Slf4j
class OneDriveService implements IcsrDriveService {

    private static Client CLIENT_OBJECT = null
    private static String AUTH_CODE = null
    static String STATIC_ACK_FILE = "CASE_ACK_FILE.xml"

    void loadOneDriveClient() {
        String clientId = Holders.config.getProperty('oneDrive.clientId');
        String[] scope = ["Files.ReadWrite.All", "offline_access"] as String[];
        String redirectURL = Holders.config.getProperty('oneDrive.redirectUrl');
        String clientSecret = Holders.config.getProperty('oneDrive.secret');
        Client client = new Client(clientId, scope, redirectURL, URLEncoder.encode(clientSecret, Constants.UTF8), false);
        CLIENT_OBJECT = client
    }

    void setAuthCode(String authCode) {
        AUTH_CODE = authCode;
        CLIENT_OBJECT.authHelper.setExternalAuthCode(authCode);
        if (authCode) {
            CLIENT_OBJECT.login()
        }

    }

    String getAuthCodeUrl() {
        return CLIENT_OBJECT.authHelper.getAuthGenerateUrl();
    }

    boolean checkOnDriveLogin() {
        return !!getOneDriveClient()?.isLogin()
    }

    private Client getOneDriveClient() {
        if (CLIENT_OBJECT && AUTH_CODE && CLIENT_OBJECT.isLogin()) {
            return CLIENT_OBJECT
        }
        log.warn("Not able to get valid CLIENT session")
        return null
    }

    void upload(String folderPath, File f) {
        Client client = getOneDriveClient()
        if (!client) {
            return
        }
        log.debug("Uploading file on OneDrive on Path ${folderPath}")
        UploadFuture future = client.uploadFile(new PathPointer(folderPath), Paths.get(f.absolutePath));
        future.syncUninterruptibly()
        log.debug("Uploaded file on OneDrive on Path ${folderPath}")
    }

    void checkIfAckReceived(String incomingFolder) {
        Client client = getOneDriveClient()
        if (!incomingFolder || !client) {
            return
        }
        log.debug("Checking ACK Files")
        FolderItem folder = client.getFolder(new PathPointer("${incomingFolder}"))
        folder.fileChildren().each {
            if (it.name == STATIC_ACK_FILE) {
                log.debug("Mark case as acknowledged")
                it.delete()
            }
        }
    }

}
