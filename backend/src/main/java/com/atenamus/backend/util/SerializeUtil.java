package com.atenamus.backend.util;

import java.util.ArrayList;

import com.atenamus.backend.MasterSecretKey;
import com.atenamus.backend.PublicKey;

import it.unisa.dia.gas.jpbc.Element;

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
}
