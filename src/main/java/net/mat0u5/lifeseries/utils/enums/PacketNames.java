package net.mat0u5.lifeseries.utils.enums;

public enum PacketNames {
    PACKET(""),

    CURSE_SLIDING("curse_sliding"),
    LIMITED_LIFE_TIMER("limited_life_timer__"),
    SESSION_TIMER("session_timer"),

    MIMICRY_COOLDOWN("mimicry_cooldown"),
    SUPERPOWER_COOLDOWN("superpower_cooldown"),
    FAKE_THUNDER("fake_thunder"),
    SNAIL_AIR("snail_air"),
    TOGGLE_TIMER("toggle_timer"),
    PREVENT_GLIDING("prevent_gliding"),
    SNAIL_TEXTURES_INFO("snail_textures_info"),
    TRIVIA_SNAIL_POS("trivia_snail_pos"),
    TRIVIA_SNAIL_PART("trivia_snail_part"),
    SNAIL_POS("snail_pos"),
    SNAIL_PART("snail_part"),
    TRIVIA_BOT_PART("trivia_bot_part"),
    SEASON_INFO("season_info"),
    SELECT_SEASON("select_season"),
    OPEN_CONFIG("open_config"),
    SELECT_WILDCARDS("select_wildcards"),
    RESET_TRIVIA("reset_trivia"),
    JUMP("jump"),
    SESSION_STATUS("session_status"),
    SNAIL_SKIN("snail_skin"),
    MORPH("morph"),
    TIME_DILATION("time_dilation"),
    PLAYER_DISGUISE("player_disguise"), // Also a packet ID
    SHOW_VIGNETTE("show_vignette"),
    PLAYER_INVISIBLE("player_invisible__"),
    TABLIST_SHOW_EXACT("tablist_show_exact"),
    CURRENT_SEASON("current_season"),
    ACTIVE_WILDCARDS("active_wildcards"),
    PLAYER_MIN_MSPT("player_min_mspt"),
    TRIPLE_JUMP("triple_jump"),
    REQUEST_SNAIL_MODEL("reset_snail_model"),
    SET_SEASON("set_season"),
    REQUEST_CONFIG("request_config"),
    SELECTED_WILDCARD("selected_wildcard"),
    TRANSCRIPT("transcript"),
    SUPERPOWER_KEY("superpower_key"),
    HOLDING_JUMP("holding_jump"),
    TRIVIA_ANSWER("trivia_answer");

    public static PacketNames fromName(String name) {
        for (PacketNames packet : PacketNames.values()) {
            if (packet.getName().equalsIgnoreCase(name)) {
                return packet;
            }
        }
        PacketNames returnPacket = PACKET;
        returnPacket.setName(name); // This changes the name of the PACKET forever, which isn't really a problem. In fact, it kinda acts as a sort of cache.
        return returnPacket;
    }


    private String name;
    PacketNames(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
