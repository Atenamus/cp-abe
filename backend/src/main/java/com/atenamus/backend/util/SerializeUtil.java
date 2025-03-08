package com.atenamus.backend.util;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import com.atenamus.backend.models.MasterSecretKey;
import com.atenamus.backend.models.PrivateKey;
import com.atenamus.backend.models.PublicKey;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.parameters.PropertiesParameters;

public class SerializeUtil {

    /**
     * Serializes an Element object to a byte array and appends it to the provided
     * list.
     * 
     * @param byteList The list to append the serialized bytes to.
     * @param element  The element to serialize.
     */
    public static void serializeElement(ArrayList<Byte> byteList, Element element) {
        byte[] elementBytes = element.toBytes();
        serializeUint32(byteList, elementBytes.length);
        appendByteArray(byteList, elementBytes);
    }

    /**
     * Serializes a string to bytes and appends it to the provided list.
     * 
     * @param byteList The list to append the serialized bytes to.
     * @param str      The string to serialize.
     */
    public static void serializeString(ArrayList<Byte> byteList, String str) {
        byte[] stringBytes = str.getBytes();
        serializeUint32(byteList, stringBytes.length);
        appendByteArray(byteList, stringBytes);
    }

    /**
     * Serializes a 32-bit unsigned integer as 4 bytes and appends it to the
     * provided list.
     * 
     * @param byteList The list to append the serialized bytes to.
     * @param value    The integer value to serialize.
     */
    private static void serializeUint32(ArrayList<Byte> byteList, int value) {
        for (int i = 3; i >= 0; i--) {
            byte b = (byte) ((value & (0x000000ff << (i * 8))) >> (i * 8));
            byteList.add(Byte.valueOf(b));
        }
    }

    /**
     * Appends a byte array to an ArrayList of Byte.
     * 
     * @param byteList The list to append the bytes to.
     * @param bytes    The byte array to append.
     */
    private static void appendByteArray(ArrayList<Byte> byteList, byte[] bytes) {
        for (byte b : bytes) {
            byteList.add(b);
        }
    }

    /**
     * Converts an ArrayList of Byte to a primitive byte array.
     * 
     * @param byteList The list of Byte objects.
     * @return A primitive byte array containing the same data.
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
     * Serializes a PublicKey object to a byte array.
     * 
     * @param pub The PublicKey to serialize.
     * @return A byte array representing the serialized PublicKey.
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
     * Serializes a MasterSecretKey object to a byte array.
     * 
     * @param msk The MasterSecretKey to serialize.
     * @return A byte array representing the serialized MasterSecretKey.
     */
    public static byte[] serializeMasterSecretKey(MasterSecretKey msk) {
        ArrayList<Byte> byteList = new ArrayList<>();
        serializeElement(byteList, msk.beta);
        serializeElement(byteList, msk.g_alpha);
        return toPrimitiveByteArray(byteList);
    }

    private static int byte2int(byte b) {
        if (b >= 0)
            return b;
        return (256 + b);
    }

    /*
     * Usage:
     * 
     * You have to do offset+=4 after call this method
     */
    /**
     * Deserializes a 32-bit unsigned integer from a byte array.
     * 
     * @param data   The byte array containing the serialized integer.
     * @param offset The offset to start deserialization from.
     * @return The deserialized integer.
     */
    private static int unserializeUint32(byte[] arr, int offset) {
        int i;
        int r = 0;

        for (i = 3; i >= 0; i--)
            r |= (byte2int(arr[offset++])) << (i * 8);
        return r;
    }

    /**
     * Deserializes a string from a byte array.
     * 
     * @param data      The byte array containing the serialized string.
     * @param offset    The offset to start deserialization from.
     * @param strBuffer A StringBuffer to store the deserialized string.
     * @return The new offset after deserialization.
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
     * Deserializes an Element from a byte array.
     * 
     * @param data    The byte array containing the serialized element.
     * @param offset  The offset to start deserialization from.
     * @param element The Element object to populate.
     * @return The new offset after deserialization.
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
     * Deserializes a PublicKey from a byte array.
     * 
     * @param data The byte array to deserialize.
     * @return The reconstructed PublicKey.
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
     * Deserializes a MasterSecretKey from a byte array using the given PublicKey
     * for pairing information.
     * 
     * @param pub  The PublicKey used to initialize the pairing and element types.
     * @param data The byte array containing the serialized MasterSecretKey.
     * @return The deserialized MasterSecretKey.
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
     * Serializes a PrivateKey object to a byte array.
     * 
     * @param privateKey The PrivateKey to serialize.
     * @return A byte array representing the serialized PrivateKey.
     */
    public static byte[] serializePrivateKey(PrivateKey privateKey) {
        ArrayList<Byte> byteList = new ArrayList<>();

        serializeElement(byteList, privateKey.d);

        int componentCount = privateKey.comps.size();
        serializeUint32(byteList, componentCount);

        for (int i = 0; i < componentCount; i++) {
            serializeString(byteList, privateKey.comps.get(i).attr);
            serializeElement(byteList, privateKey.comps.get(i).d);
            serializeElement(byteList, privateKey.comps.get(i).dp);
        }

        return toPrimitiveByteArray(byteList);
    }
}
