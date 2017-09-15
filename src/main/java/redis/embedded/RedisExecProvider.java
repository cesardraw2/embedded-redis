package redis.embedded;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import redis.embedded.util.Architecture;
import redis.embedded.util.JarUtil;
import redis.embedded.util.OS;
import redis.embedded.util.OsArchitecture;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class RedisExecProvider {

    private final Map<OsArchitecture, String> executables = Maps.newHashMap();

    /**
     * @return a new RedisExecProvider instance
     * @deprecated use {@link #build()} instead
     */
    @Deprecated
    public static RedisExecProvider defaultProvider() {
        return new RedisExecProvider();
    }

    public static RedisExecProvider build() {
        return new RedisExecProvider();
    }

    private RedisExecProvider() {
        initExecutables();
    }

    private void initExecutables() {
        executables.put(OsArchitecture.UNIX_x86_64, "redis-server-3.2.1");

        executables.put(OsArchitecture.WINDOWS_x86, "redis-server-3.2.1.exe");
        executables.put(OsArchitecture.WINDOWS_x86_64, "redis-server-3.2.1.exe");

        executables.put(OsArchitecture.MAC_OS_X_x86, "redis-server-3.2.1-mac86");
        executables.put(OsArchitecture.MAC_OS_X_x86_64, "redis-server-3.2.1-mac");
    }

    public RedisExecProvider override(OS os, String executable) {
        Preconditions.checkNotNull(executable);
        for (Architecture arch : Architecture.values()) {
            override(os, arch, executable);
        }
        return this;
    }

    public RedisExecProvider override(OS os, Architecture arch, String executable) {
        Preconditions.checkNotNull(executable);
        executables.put(new OsArchitecture(os, arch), executable);
        return this;
    }

    public File get() throws IOException {
        OsArchitecture osArch = OsArchitecture.detect();
        String executablePath = executables.get(osArch);
        return fileExists(executablePath) ?
                new File(executablePath) :
                JarUtil.extractExecutableFromJar(executablePath);

    }

    public RedisExecProvider copy() {
        RedisExecProvider copy = new RedisExecProvider();

        for (OsArchitecture k : executables.keySet()) {
            copy.override(k.os(), k.arch(), executables.get(k));
        }

        return copy;
    }

    private boolean fileExists(String executablePath) {
        return new File(executablePath).exists();
    }
}
