package com.rxlogix.file

import com.rxlogix.dto.FileDTO
import com.rxlogix.mapping.ArgusFile

class ArgusFileAttachmentService implements FileAttachmentService {

    @Override
    FileDTO getFile(String uniqueId) {
        if (!uniqueId || !uniqueId.contains('_')) {
            return null
        }
        ArgusFile.withNewSession {
            ArgusFile argusFile = ArgusFile.findByCaseIdAndSeqNum(uniqueId.split('_').first().toLong(), uniqueId.split('_').last().toLong())
            return new FileDTO(name: argusFile.fileName, data: argusFile?.data)
        }
    }

}
