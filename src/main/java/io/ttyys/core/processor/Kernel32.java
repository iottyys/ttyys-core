package io.ttyys.core.processor;

import com.sun.jna.Library;
import com.sun.jna.Native;

/**
 * Kernel32
 *
 * @author tengwang
 * @version 1.0.0
 * @date 2021/3/22 1:17 下午
 */
public interface Kernel32 extends Library {

    public static Kernel32 INSTANCE = (Kernel32) Native.loadLibrary("kernel32", Kernel32.class);

    public long GetProcessId(Long hProcess);
}