package com.atenamus.backend.util;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import com.atenamus.backend.models.Cipher;
import com.atenamus.backend.models.MasterSecretKey;
import com.atenamus.backend.models.Policy;
import com.atenamus.backend.models.PrivateKey;
import com.atenamus.backend.models.PrivateKeyComp;
import com.atenamus.backend.models.PublicKey;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.parameters.PropertiesParameters;

/**
 * Utility class for serializing and deserializing cryptographic objects used in the CP-ABE scheme.
 * This class provides methods to convert complex cryptographic objects to byte arrays and back.
 */
public class SerializeUtil {

    // Serialization Methods

    /**
     * Serializes a cryptographic element to a byte list.
     * 
     * @param byteList The byte list to append the serialized element to.
     * @param element The cryptographic element to serialize.
     */
    public static void serializeElement(ArrayList<Byte> byteList, Element element) {
        byte[] elementBytes = element.toBytes();
        serializeUint32(byteList, elementBytes.length);
        appendByteArray(byteList, elementBytes);
    }

    /**
     * Serializes a string to a byte list, prefixed with its length.
     * 
     * @param byteList The byte list to append the serialized string to.
     * @param str The string to serialize.
     */
    public static void serializeString(ArrayList<Byte> byteList, String str) {
        byte[] stringBytes = str.getBytes();
        serializeUint32(byteList, stringBytes.length);
        appendByteArray(byteList, stringBytes);
    }

    /**
     * Serializes a Policy object into an ArrayList of Bytes.
     *
     * @param arrlist The ArrayList of Bytes where the serialized data will be stored.
     * @param p The Policy object to be serialized.
     */
    private static void serializePolicy(ArrayList<Byte> arrlist, Policy p) {
        serializeUint32(arrlist, p.k);

        if (p.children == null || p.children.length == 0) {
            serializeUint32(arrlist, 0);
            serializeString(arrlist, p.attr);
            serializeElement(arrlist, p.c);
            serializeElement(arrlist, p.cp);
        } else {
            serializeUint32(arrlist, p.children.length);
            for (int i = 0; i < p.children.length; i++)
                serializePolicy(arrlist, p.children[i]);
        }
    }

    /**
     * Serializes a 32-bit unsigned integer to a byte list in big-endian format.
     * 
     * @param byteList The byte list to append the serialized integer to.
     * @param value The 32-bit integer to serialize.
     */
    private static void serializeUint32(ArrayList<Byte> byteList, int value) {
        for (int i = 3; i >= 0; i--) {
            byte b = (byte) ((value & (0x000000ff << (i * 8))) >> (i * 8));
            byteList.add(Byte.valueOf(b));
        }
    }

    private static void serializeLong(ArrayList<Byte> byteList, long value) {
        for (int i = 7; i >= 0; i--) {
            byteList.add((byte) ((value >> (i * 8)) & 0xFF));
        }
    }

    /**
     * Appends a byte array to a byte list.
     * 
     * @param byteList The byte list to append to.
     * @param bytes The byte array to append.
     */
    private static void appendByteArray(ArrayList<Byte> byteList, byte[] bytes) {
        for (byte b : bytes) {
            byteList.add(b);
        }
    }

    /**
     * Converts an ArrayList of Byte objects to a primitive byte array.
     * 
     * @param byteList The ArrayList of Byte objects to convert.
     * @return A primitive byte array containing the same bytes.
     */
    private static byte[] toPrimitiveByteArray(ArrayList<Byte> byteList) {
        int len = byteList.size();
        byte[] byteArray = new byte[len];
        for (int i = 0; i < len; i++) {
            byteArray[i] = byteList.get(i);
        }
        return byteArray;
    }

    /**
     * Serializes a PublicKey object to a byte array. Includes the pairing description and all
     * cryptographic elements.
     * 
     * @param pub The PublicKey object to serialize.
     * @return A byte array containing the serialized public key.
     */
    public static byte[] serializePublicKey(PublicKey pub) {
        ArrayList<Byte> byteList = new ArrayList<>();
        serializeString(byteList, pub.pairingDesc);
        serializeElement(byteList, pub.g);
        serializeElement(byteList, pub.h);
        serializeElement(byteList, pub.gp);
        serializeElement(byteList, pub.g_hat_alpha);
        return toPrimitiveByteArray(byteList);
    }

    /**
     * Serializes a MasterSecretKey object to a byte array. Includes the beta and g_alpha elements.
     * 
     * @param msk The MasterSecretKey object to serialize.
     * @return A byte array containing the serialized master secret key.
     */
    public static byte[] serializeMasterSecretKey(MasterSecretKey msk) {
        ArrayList<Byte> byteList = new ArrayList<>();
        serializeElement(byteList, msk.beta);
        serializeElement(byteList, msk.g_alpha);
        return toPrimitiveByteArray(byteList);
    }

    /**
     * Serializes a PrivateKey object to a byte array. Includes the d element and all key components
     * with their attributes.
     * 
     * @param privateKey The PrivateKey object to serialize.
     * @return A byte array containing the serialized private key.
     */
    public static byte[] serializePrivateKey(PrivateKey privateKey) {
        ArrayList<Byte> byteList = new ArrayList<>();
        serializeElement(byteList, privateKey.d);

        // Serialize traceability information
        serializeString(byteList, privateKey.userId != null ? privateKey.userId : "");
        serializeString(byteList, privateKey.userEmail != null ? privateKey.userEmail : "");
        serializeLong(byteList, privateKey.timestamp);
        serializeLong(byteList, privateKey.expirationDate);

        int componentCount = privateKey.comps.size();
        serializeUint32(byteList, componentCount);
        for (int i = 0; i < componentCount; i++) {
            serializeString(byteList, privateKey.comps.get(i).attr);
            serializeElement(byteList, privateKey.comps.get(i).d);
            serializeElement(byteList, privateKey.comps.get(i).dp);
        }
        return toPrimitiveByteArray(byteList);
    }

    /**
     * Serializes a Cipher object into a byte array.
     *
     * @param cph the Cipher object to be serialized
     * @return a byte array representing the serialized Cipher object
     */
    public static byte[] serializeCipher(Cipher cph) {
        ArrayList<Byte> arrlist = new ArrayList<Byte>();
        SerializeUtil.serializeElement(arrlist, cph.cs);
        SerializeUtil.serializeElement(arrlist, cph.c);

        // Serialize encryption date
        serializeUint32(arrlist, (int) (cph.encryptionDate & 0xFFFFFFFF));
        serializeUint32(arrlist, (int) (cph.encryptionDate >>> 32));

        SerializeUtil.serializePolicy(arrlist, cph.p);

        return byte_arr2byte(arrlist);
    }

    /**
     * Converts an ArrayList of Byte objects to a byte array.
     *
     * @param B the ArrayList of Byte objects to be converted
     * @return a byte array containing the same elements as the input ArrayList
     */
    private static byte[] byte_arr2byte(ArrayList<Byte> B) {
        int len = B.size();
        byte[] b = new byte[len];

        for (int i = 0; i < len; i++)
            b[i] = B.get(i).byteValue();

        return b;
    }

    // Deserialization Methods

    /**
     * Converts a byte to an unsigned integer value (0-255).
     * 
     * @param b The byte to convert.
     * @return The unsigned integer value of the byte.
     */
    private static int byte2int(byte b) {
        if (b >= 0)
            return b;
        return (256 + b);
    }

    /**
     * Deserializes a 32-bit unsigned integer from a byte array at the specified offset.
     * 
     * @param arr The byte array containing the serialized integer.
     * @param offset The offset in the byte array to start reading from.
     * @return The deserialized 32-bit integer.
     */
    private static int unserializeUint32(byte[] arr, int offset) {
        int i;
        int r = 0;
        for (i = 3; i >= 0; i--)
            r |= (byte2int(arr[offset++])) << (i * 8);
        return r;
    }

    private static long deserializeLong(byte[] arr, int offset) {
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value = (value << 8) | (arr[offset + i] & 0xFF);
        }
        return value;
    }

    /**
     * Deserializes a string from a byte array at the specified offset.
     * 
     * @param data The byte array containing the serialized string.
     * @param offset The offset in the byte array to start reading from.
     * @param strBuffer The StringBuffer to store the deserialized string.
     * @return The updated offset after reading the string.
     */
    public static int unserializeString(byte[] data, int offset, StringBuffer strBuffer) {
        int length = unserializeUint32(data, offset);
        offset += 4; // Move past length bytes
        byte[] stringBytes = new byte[length];
        System.arraycopy(data, offset, stringBytes, 0, length);
        strBuffer.append(new String(stringBytes));
        return offset + length;
    }

    /**
     * Deserializes a cryptographic element from a byte array at the specified offset.
     * 
     * @param data The byte array containing the serialized element.
     * @param offset The offset in the byte array to start reading from.
     * @param element The Element object to populate with deserialized data.
     * @return The updated offset after reading the element.
     */
    public static int unserializeElement(byte[] data, int offset, Element element) {
        int length = unserializeUint32(data, offset);
        offset += 4;
        byte[] elementBytes = new byte[length];
        System.arraycopy(data, offset, elementBytes, 0, length);
        element.setFromBytes(elementBytes);
        return offset + length;
    }

    /**
     * Deserializes a PublicKey object from a byte array. Reconstructs the pairing and all
     * cryptographic elements.
     * 
     * @param data The byte array containing the serialized public key.
     * @return The deserialized PublicKey object.
     */
    public static PublicKey unserializePublicKey(byte[] data) {
        PublicKey pub = new PublicKey();
        int offset = 0;
        StringBuffer pairingDescBuffer = new StringBuffer();
        offset = unserializeString(data, offset, pairingDescBuffer);
        pub.pairingDesc = pairingDescBuffer.toString();
        PropertiesParameters params = new PropertiesParameters();
        params.load(new ByteArrayInputStream(pub.pairingDesc.getBytes()));
        pub.p = PairingFactory.getPairing(params);
        Pairing pairing = pub.p;
        pub.g = pairing.getG1().newElement();
        pub.h = pairing.getG1().newElement();
        pub.gp = pairing.getG2().newElement();
        pub.g_hat_alpha = pairing.getGT().newElement();
        offset = unserializeElement(data, offset, pub.g);
        offset = unserializeElement(data, offset, pub.h);
        offset = unserializeElement(data, offset, pub.gp);
        unserializeElement(data, offset, pub.g_hat_alpha);
        return pub;
    }

    /**
     * Deserializes a MasterSecretKey object from a byte array. Requires the corresponding PublicKey
     * to initialize the appropriate field elements.
     * 
     * @param pub The PublicKey associated with this master secret key.
     * @param data The byte array containing the serialized master secret key.
     * @return The deserialized MasterSecretKey object.
     */
    public static MasterSecretKey unserializeMasterSecretKey(PublicKey pub, byte[] data) {
        int offset = 0;
        MasterSecretKey masterSecretKey = new MasterSecretKey();
        masterSecretKey.beta = pub.p.getZr().newElement();
        masterSecretKey.g_alpha = pub.p.getG2().newElement();
        offset = unserializeElement(data, offset, masterSecretKey.beta);
        unserializeElement(data, offset, masterSecretKey.g_alpha);
        return masterSecretKey;
    }

    /**
     * Deserializes a Cipher object from a byte array. Requires the corresponding PublicKey to
     * initialize the appropriate field elements.
     * 
     * @param pub The PublicKey associated with this cipher.
     * @param cphBuf The byte array containing the serialized cipher.
     * @return The deserialized Cipher object.
     */
    public static Cipher unserializeCipher(PublicKey pub, byte[] cphBuf) {
        Cipher cph = new Cipher();
        int offset = 0;
        int[] offset_arr = new int[1];
        cph.cs = pub.p.getGT().newElement();
        cph.c = pub.p.getG1().newElement();
        offset = SerializeUtil.unserializeElement(cphBuf, offset, cph.cs);
        offset = SerializeUtil.unserializeElement(cphBuf, offset, cph.c);

        // Deserialize encryption date
        long encryptionDateLow = unserializeUint32(cphBuf, offset);
        offset += 4;
        long encryptionDateHigh = unserializeUint32(cphBuf, offset);
        offset += 4;
        cph.encryptionDate = (encryptionDateHigh << 32) | encryptionDateLow;

        offset_arr[0] = offset;
        cph.p = SerializeUtil.unserializePolicy(pub, cphBuf, offset_arr);
        offset = offset_arr[0];
        return cph;
    }

    /**
     * Recursively deserializes a Policy object from a byte array. Policies can be nested and form a
     * tree structure.
     * 
     * @param pub The PublicKey associated with this policy.
     * @param cphBuf The byte array containing the serialized policy.
     * @param offset_arr Single-element array containing the current offset in the byte array, used
     *        for recursive deserialization.
     * @return The deserialized Policy object.
     */
    private static Policy unserializePolicy(PublicKey pub, byte[] cphBuf, int[] offset_arr) {
        int i;
        int n;
        Policy p = new Policy();
        p.k = unserializeUint32(cphBuf, offset_arr[0]);
        offset_arr[0] += 4;
        p.attr = null;
        n = unserializeUint32(cphBuf, offset_arr[0]);
        offset_arr[0] += 4;
        if (n == 0) {
            // Leaf node case - has an attribute and cryptographic elements
            p.children = null;
            StringBuffer sb = new StringBuffer("");
            offset_arr[0] = unserializeString(cphBuf, offset_arr[0], sb);
            p.attr = sb.substring(0);
            p.c = pub.p.getG1().newElement();
            p.cp = pub.p.getG1().newElement();
            offset_arr[0] = unserializeElement(cphBuf, offset_arr[0], p.c);
            offset_arr[0] = unserializeElement(cphBuf, offset_arr[0], p.cp);
        } else {
            // Internal node case - has children
            p.children = new Policy[n];
            for (i = 0; i < n; i++)
                p.children[i] = unserializePolicy(pub, cphBuf, offset_arr);
        }
        return p;
    }

    public static PrivateKey unserializePrivateKey(PublicKey pub, byte[] prvBytes) {
        PrivateKey prv = new PrivateKey();
        int offset = 0;

        prv.d = pub.p.getG2().newElement();
        offset = unserializeElement(prvBytes, offset, prv.d);

        // Deserialize traceability information
        StringBuffer userIdBuffer = new StringBuffer();
        offset = unserializeString(prvBytes, offset, userIdBuffer);
        prv.userId = userIdBuffer.toString();

        StringBuffer userEmailBuffer = new StringBuffer();
        offset = unserializeString(prvBytes, offset, userEmailBuffer);
        prv.userEmail = userEmailBuffer.toString();

        prv.timestamp = deserializeLong(prvBytes, offset);
        offset += 8;
        prv.expirationDate = deserializeLong(prvBytes, offset);
        offset += 8;

        prv.comps = new ArrayList<PrivateKeyComp>();
        int len = unserializeUint32(prvBytes, offset);
        offset += 4;

        for (int i = 0; i < len; i++) {
            PrivateKeyComp c = new PrivateKeyComp();

            StringBuffer sb = new StringBuffer("");
            offset = unserializeString(prvBytes, offset, sb);
            c.attr = sb.substring(0);

            c.d = pub.p.getG2().newElement();
            c.dp = pub.p.getG2().newElement();

            offset = unserializeElement(prvBytes, offset, c.d);
            offset = unserializeElement(prvBytes, offset, c.dp);

            prv.comps.add(c);
        }

        return prv;
    }
}
