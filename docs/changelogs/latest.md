# Full changelog from version `1.3.7.2` to `*`.

----------

'wildcard_hunger_randomize_interval', 'wildcard_mobswap_start_spawn_delay' and 'wildcard_mobswap_end_spawn_delay' configs are now ACTUALLY in seconds, not in ticks.

----------

Fixed some scaled textures not being properly cut off.<br>
When a zombie gets revived in Wild Life by killing a dark green, the message that shows they gained a life now properly shows up.<br>
Added the 'wildcard_superpowers_disable_intro_theme' Wild Life config. Default value is false.

----------

Added the ability to set your in-game hearts color based on the number of lives you have (or rather your team color).<br>
This is controlled by the clien-side 'colored_hearts' config. Default value is false.<br>
Works with all 16 possible team colors.<br>
Not compatible with Secret Life.<br>
Added the 'colored_hearts_hardcore_last_life' client-side config. Default value is true.<br>
Added the 'colored_hearts_hardcore_all_lives' client-side config. Default value is false.

----------

Watchers can no longer be soulmates.<br>
Zombies no longer lose their items in between revives.<br>
Zombies no longer sometimes spawn without full health

----------

Added the StringListConfigEntry config type, which only allows certain string values in its list. (similar to item/block config entries, just more flexible)<br>
Added the 'wildcard_callback_wildcards_blacklist' Wild Life config entry. Default value is '[hunger]'.<br>
Fixed a bug in the config gui where text field config entries sometimes would not allow you to save changes even if they didn't have an error value.

----------

Snails and Trivia Bots can no longer go through nether (and end) portals.<br>
Added the 'wildcard_superpowers_power_blacklist' Wild Life config, which allows you to prevent some powers from showing up from randomizations. Default value is '[]'.

----------

Added the 'breakup_last_pair_standing' Double Life config. Default value is false.<br>
Added the 'disable_start_teleport' Double Life config. Default value is false.<br>
Added the 'soulbound_locator_bar' Double Life config. Default value is false.