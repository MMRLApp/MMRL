package com.dergoogler.mmrl.platform.file

enum class SuFilePermissions(
    val value: Int,
) {
    /**
     * `0o400 (256) - r-- for owner`
     */
    OWNER_READ(0b100000000),

    /**
     * `0o200 (128) - -w- for owner`
     */
    OWNER_WRITE(0b010000000),

    /**
     * `0o100 (64) - --x for owner`
     */
    OWNER_EXECUTE(0b001000000),

    /**
     * `0o040 (32) - r-- for group`
     */
    GROUP_READ(0b000100000),

    /**
     * `0o020 (16) - -w- for group`
     */
    GROUP_WRITE(0b000010000),

    /**
     * `0o010 (8) - --x for group`
     */
    GROUP_EXECUTE(0b000001000),

    /**
     * `0o004 (4) - r-- for others`
     */
    OTHERS_READ(0b000000100),

    /**
     * `0o002 (2) - -w- for others`
     */
    OTHERS_WRITE(0b000000010),

    /**
     * `0o001 (1) - --x for others`
     */
    OTHERS_EXECUTE(0b000000001),

    /**
     * `0o777 (511) - rwxrwxrwx`
     */
    PERMISSION_777(0b111111111),

    /**
     * `0o755 (493) - rwxr-xr-x`
     */
    PERMISSION_755(0b111101101),

    /**
     * `0o744 (484) - rwxr--r--`
     */
    PERMISSION_700(0b111000000),

    /**
     * `0o666 (442) - rw-rw-rw-`
     */
    PERMISSION_644(0b110100100),

    /**
     * `0o600 (384) - rw-------`
     */
    PERMISSION_600(0b110000000),

    /**
     * `0o444 (292) - r--r--r--`
     */
    PERMISSION_444(0b100100100),
    ;

    companion object {
        fun combine(vararg permissions: SuFilePermissions): Int = permissions.fold(0) { acc, perm -> acc or perm.value }
    }
}
