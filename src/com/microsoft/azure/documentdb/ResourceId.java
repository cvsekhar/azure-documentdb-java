package com.microsoft.azure.documentdb;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

class ResourceId {
    static final short Length = 20;

    private int database;
    private int documentCollection;
    private long storedProcedure;
    private long trigger;
    private long userDefinedFunction;
    private long conflict;
    private long document;
    private int user;
    private long permission;
    private int attachment;

    private ResourceId() {
        this.database = 0;
        this.documentCollection = 0;
        this.storedProcedure = 0;
        this.trigger = 0;
        this.userDefinedFunction = 0;
        this.document = 0;
        this.user = 0;
        this.conflict = 0;
        this.permission = 0;
        this.attachment = 0;
    }

    public int getDatabase() {
        return this.database;
    }

    public ResourceId getDatabaseId() {
        ResourceId rid = new ResourceId();
        rid.database = this.database;
        return rid;
    }

    public int getDocumentCollection() {
        return this.documentCollection;
    }

    public ResourceId getDocumentCollectionId() {
        ResourceId rid = new ResourceId();
        rid.database = this.database;
        rid.documentCollection = this.documentCollection;
        return rid;
    }

    /**
     * Unique (across all databases) Id for the DocumentCollection.
     * First 4 bytes are DatabaseId and next 4 bytes are CollectionId.
     * @return the unique collectionId
     */
    public long getUniqueDocumentCollectionId() {
        return (long) this.database << 32 | this.documentCollection;
    }

    public long getStoredProcedure() {
        return this.storedProcedure;
    }

    public ResourceId getStoredProcedureId() {
        ResourceId rid = new ResourceId();
        rid.database = this.database;
        rid.documentCollection = this.documentCollection;
        rid.storedProcedure = this.storedProcedure;
        return rid;
    }

    public long getTrigger() {
        return this.trigger;
    }

    public ResourceId getTriggerId() {
        ResourceId rid = new ResourceId();
        rid.database = this.database;
        rid.documentCollection = this.documentCollection;
        rid.trigger = this.trigger;
        return rid;
    }

    public long getUserDefinedFunction() {
        return this.userDefinedFunction;
    }

    public ResourceId getUserDefinedFunctionId() {
        ResourceId rid = new ResourceId();
        rid.database = this.database;
        rid.documentCollection = this.documentCollection;
        rid.userDefinedFunction = this.userDefinedFunction;
        return rid;
    }

    public long getConflict() {
        return this.conflict;
    }

    public ResourceId getConflictId() {
        ResourceId rid = new ResourceId();
        rid.database = this.database;
        rid.documentCollection = this.documentCollection;
        rid.conflict = this.conflict;
        return rid;
    }

    public long getDocument() {
        return this.document;
    }

    public ResourceId getDocumentId() {
        ResourceId rid = new ResourceId();
        rid.database = this.database;
        rid.documentCollection = this.documentCollection;
        rid.document = this.document;
        return rid;
    }

    public int getUser() {
        return this.user;
    }

    public ResourceId getUserId() {
        ResourceId rid = new ResourceId();
        rid.database = this.database;
        rid.user = this.user;
        return rid;
    }

    public long getPermission() {
        return this.permission;
    }

    public ResourceId getPermissionId() {
        ResourceId rid = new ResourceId();
        rid.database = this.database;
        rid.user = this.user;
        rid.permission = this.permission;
        return rid;
    }

    public int getAttachment() {
        return this.attachment;
    }

    public ResourceId getAttachmentId() {
        ResourceId rid = new ResourceId();
        rid.database = this.database;
        rid.documentCollection = this.documentCollection;
        rid.document = this.document;
        rid.attachment = this.attachment;
        return rid;
    }

    public byte[] getValue() {
        int len = 0;
        if (this.database != 0)
            len += 4;
        if (this.documentCollection != 0 || this.user != 0)
            len += 4;
        if (this.document != 0 || this.permission != 0
                || this.storedProcedure != 0 || this.trigger != 0
                || this.userDefinedFunction != 0 || this.conflict != 0)
            len += 8;
        if (this.attachment != 0)
            len += 4;

        byte[] val = new byte[len];

        if (this.database != 0)
            ResourceId.blockCopy(convertToBytesUsingByteBuffer(this.database),
                    0, val, 0, 4);

        if (this.documentCollection != 0)
            ResourceId.blockCopy(
                    convertToBytesUsingByteBuffer(this.documentCollection), 0,
                    val, 4, 4);
        else if (this.user > 0)
            ResourceId.blockCopy(convertToBytesUsingByteBuffer(this.user), 0,
                    val, 4, 4);

        if (this.storedProcedure != 0)
            ResourceId.blockCopy(
                    convertToBytesUsingByteBuffer(this.storedProcedure), 0,
                    val, 8, 8);
        if (this.trigger != 0)
            ResourceId.blockCopy(convertToBytesUsingByteBuffer(this.trigger),
                    0, val, 8, 8);
        if (this.userDefinedFunction != 0)
            ResourceId.blockCopy(
                    convertToBytesUsingByteBuffer(this.userDefinedFunction), 0,
                    val, 8, 8);
        if (this.conflict != 0)
            ResourceId.blockCopy(convertToBytesUsingByteBuffer(this.conflict),
                    0, val, 8, 8);
        else if (this.document != 0)
            ResourceId.blockCopy(convertToBytesUsingByteBuffer(this.document),
                    0, val, 8, 8);
        else if (this.permission != 0)
            ResourceId.blockCopy(
                    convertToBytesUsingByteBuffer(this.permission), 0, val, 8,
                    8);

        if (this.attachment != 0)
            ResourceId.blockCopy(
                    convertToBytesUsingByteBuffer(this.attachment), 0, val, 16,
                    4);

        return val;
    }

    public static ResourceId parse(String id) throws IllegalArgumentException {
        Pair<Boolean, ResourceId> pair = ResourceId.tryParse(id);

        if (!pair.getKey()) {
            throw new IllegalArgumentException(String.format(
                "Invalide resourceid %s", id)); 
        }
        return pair.getValue();
    }

    public static ResourceId newDatabaseId(int dbid) {
        ResourceId resourceId = new ResourceId();
        resourceId.database = dbid;
        return resourceId;
    }

    public static ResourceId newDocumentCollectionId(String databaseId,
            int collectionId) throws DocumentClientException {
        ResourceId dbId = ResourceId.parse(databaseId);

        ResourceId collectionResourceId = new ResourceId();
        collectionResourceId.database = dbId.database;
        collectionResourceId.documentCollection = collectionId;

        return collectionResourceId;
    }

    public static ResourceId newUserId(String databaseId, int userId) 
            throws DocumentClientException {
        ResourceId dbId = ResourceId.parse(databaseId);

        ResourceId userResourceId = new ResourceId();
        userResourceId.database = dbId.database;
        userResourceId.user = userId;

        return userResourceId;
    }

    public static ResourceId newPermissionId(String userId, long permissionId) 
            throws DocumentClientException {
        ResourceId usrId = ResourceId.parse(userId);

        ResourceId permissionResourceId = new ResourceId();
        permissionResourceId.database = usrId.database;
        permissionResourceId.user = usrId.user;
        permissionResourceId.permission = permissionId;
        return permissionResourceId;
    }

    public static ResourceId newAttachmentId(String documentId, int attachmentId) 
            throws DocumentClientException {
        ResourceId docId = ResourceId.parse(documentId);

        ResourceId attachmentResourceId = new ResourceId();
        attachmentResourceId.database = docId.database;
        attachmentResourceId.documentCollection = docId.documentCollection;
        attachmentResourceId.document = docId.document;
        attachmentResourceId.attachment = attachmentId;

        return attachmentResourceId;
    }

    public static Pair<Boolean, ResourceId> tryParse(String id) {
        ResourceId rid = null;

        try {
            if (StringUtils.isEmpty(id))
                return Pair.of(false, null);

            byte[] buffer = null;

            Pair<Boolean, byte[]> pair = ResourceId.verify(id);

            buffer = pair.getValue();
            if (pair.getKey() == false)
                return Pair.of(false, null);

            rid = new ResourceId();

            if (buffer.length >= 4)
                rid.database = ByteBuffer.wrap(buffer).getInt();

            if (buffer.length >= 8) {
                byte[] temp = new byte[4];
                ResourceId.blockCopy(buffer, 4, temp, 0, 4);

                boolean isCollection = (temp[0] & (128)) > 0 ? true : false;

                if (isCollection) {
                    rid.documentCollection = ByteBuffer.wrap(temp).getInt();

                    if (buffer.length >= 16) {
                        byte[] subCollRes = new byte[8];
                        ResourceId.blockCopy(buffer, 8, subCollRes, 0, 8);

                        long subCollectionResource = ByteBuffer.wrap(buffer, 8,
                                8).getLong();
                        if ((subCollRes[7] >> 4) == (byte) CollectionChildResourceType.Document) {
                            rid.document = subCollectionResource;

                            if (buffer.length == 20) {
                                rid.attachment = ByteBuffer.wrap(buffer, 16, 4)
                                        .getInt();
                            }
                        } else if (Math.abs(subCollRes[7] >> 4) == (byte) CollectionChildResourceType.StoredProcedure) {
                            rid.storedProcedure = subCollectionResource;
                        } else if ((subCollRes[7] >> 4) == (byte) CollectionChildResourceType.Trigger) {
                            rid.trigger = subCollectionResource;
                        } else if ((subCollRes[7] >> 4) == (byte) CollectionChildResourceType.UserDefinedFunction) {
                            rid.userDefinedFunction = subCollectionResource;
                        } else if ((subCollRes[7] >> 4) == (byte) CollectionChildResourceType.Conflict) {
                            rid.conflict = subCollectionResource;
                        } else {
                            return Pair.of(false, rid);
                        }
                    } else if (buffer.length != 8) {
                        return Pair.of(false, rid);
                    }
                } else {
                    rid.user = ByteBuffer.wrap(temp).getInt();

                    if (buffer.length == 16) {
                        rid.permission = ByteBuffer.wrap(buffer, 8, 8)
                                .getLong();
                    } else if (buffer.length != 8) {
                        return Pair.of(false, rid);
                    }
                }
            }

            return Pair.of(true, rid);
        } catch (Exception e) {
            return Pair.of(false, null);
        }
    }

    public static Pair<Boolean, byte[]> verify(String id) {
        if (StringUtils.isEmpty(id))
            throw new IllegalArgumentException("id");

        byte[] buffer = null;

        try {
            buffer = ResourceId.fromBase64String(id);
        } catch (Exception e) {
        }

        if (buffer == null || buffer.length > ResourceId.Length) {
            buffer = null;
            return Pair.of(false, buffer);
        }

        return Pair.of(true, buffer);
    }

    public static boolean verifyBool(String id) {
        return verify(id).getKey();
    }

    public String toString() {
        return ResourceId.toBase64String(this.getValue());
    }

    static byte[] fromBase64String(String s) {
        return Base64.decodeBase64(s.replace('-', '/'));
    }

    static String toBase64String(byte[] buffer) {
        return ResourceId.toBase64String(buffer, 0, buffer.length);
    }

    static String toBase64String(byte[] buffer, int offset, int length) {
        byte[] subBuffer = Arrays.copyOfRange(buffer, offset, length);

        return Utils.encodeBase64String(subBuffer).replace('/', '-');
    }

    // Copy the bytes provided with a for loop, faster when there are only a few
    // bytes to copy
    static void blockCopy(byte[] src, int srcOffset, byte[] dst, int dstOffset,
            int count) {
        int stop = srcOffset + count;
        for (int i = srcOffset; i < stop; i++)
            dst[dstOffset++] = src[i];
    }

    private static byte[] convertToBytesUsingByteBuffer(int value) {
        ByteOrder order = ByteOrder.BIG_ENDIAN;
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(order);
        return buffer.putInt(value).array();
    }

    private static byte[] convertToBytesUsingByteBuffer(long value) {
        ByteOrder order = ByteOrder.BIG_ENDIAN;
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.order(order);
        return buffer.putLong(value).array();
    }

    // Using a byte however, we only need nibble here.
    private class CollectionChildResourceType {
        public static final byte Document = 0x0;
        public static final byte StoredProcedure = 0x08;
        public static final byte Trigger = 0x07;
        public static final byte UserDefinedFunction = 0x06;
        public static final byte Conflict = 0x04;
    }
}
