package org.flybit.account;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.logging.Logger;

import org.flybit.crypto.codec.Base58;
import org.flybit.crypto.codec.CRC16;
import org.flybit.crypto.Crypto;


public class AccountUtil {

    private static final Logger logger = Logger.getLogger(AccountUtil.class.getName());

    public static String addressFromPublicKey(byte[] publicKey, AccountType accountType) {
        final ByteBuffer bb = ByteBuffer.allocate(33);
        bb.put(accountType.code());
        bb.put(publicKey);

        String address = addressFromBytes(bb.array());
        address = accountType.code()+address;
        return address;

    }

    public static String addressFromBytes(byte[] bytes) {
        final byte[] ripemd160Bytes = Crypto.ripemd160Digest(bytes);
        final ByteBuffer buffer = ByteBuffer.allocate(24);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(ripemd160Bytes);
        buffer.putInt(checksum(ripemd160Bytes));

        final String address = Base58.encode(buffer.array());
        return address;

    }

    private static int checksum(byte[] bytes) {
        final CRC16 checksum = new CRC16();

        // update the current checksum with the specified array of bytes
        checksum.update(bytes, 0, bytes.length);

        // get the current checksum value
        final int checksumValue = checksum.getValue();
        return checksumValue;
    }

    public static boolean validateAddress(String address, AccountType accountType) {
        final int code = Integer.valueOf(address.substring(0, 1));
        if(AccountType.fromCode((byte)code) != accountType)
            return false;
        return validateAddress(address.substring(1));
    }

    public static boolean validateAddress(String address) {
        final byte[] decodedBytes = Base58.decode(address);
        if (decodedBytes == null) return false;
        final ByteBuffer buffer = ByteBuffer.wrap(Arrays.copyOfRange(decodedBytes, decodedBytes.length-4, decodedBytes.length));
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        final int checksum = buffer.getInt();

        final int checksumCaculated = checksum(Arrays.copyOfRange(decodedBytes, 0, decodedBytes.length-4));

        return checksum == checksumCaculated;

    }

}
