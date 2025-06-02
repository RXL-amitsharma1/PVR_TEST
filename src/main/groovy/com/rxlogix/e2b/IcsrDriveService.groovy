package com.rxlogix.e2b

interface IcsrDriveService {

    abstract void upload(String folderPath, File f);

    abstract void checkIfAckReceived(String incomingFolder);

}
