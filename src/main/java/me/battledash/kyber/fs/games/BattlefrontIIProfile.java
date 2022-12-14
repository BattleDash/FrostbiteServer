package me.battledash.kyber.fs.games;

import lombok.Getter;
import me.battledash.kyber.fs.GameProfile;
import me.battledash.kyber.util.Fnv1;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class BattlefrontIIProfile implements GameProfile {

    @Getter
    private final Map<Long, String> sharedBundles = new LinkedHashMap<>();

    {
        String[] bundles = {
                "win32/gameplay/bundles/sharedbundles/frontend+mp/abilities/sharedbundleabilities_frontend+mp",
                "win32/gameplay/bundles/sharedbundles/common/animation/sharedbundleanimation_common",
                "win32/gameplay/bundles/sharedbundles/frontend+mp/characters/sharedbundlecharacters_frontend+mp",
                "win32/gameplay/bundles/sharedbundles/common/vehicles/sharedbundlevehiclescockpits",
                "win32/gameplay/bundles/sharedbundles/common/characters/sharedbundlecharacters1p",
                "win32/ui/frontend/webbrowser/webbrowserresourcebundle",
                "win32/systems/frostbitestartupdata",
                "win32/gameplay/wrgameconfiguration",
                "win32/default_settings",
                "win32/s1/gameplay/bundles/sharedbundleseason1",
                "win32/gameplay/bundles/sp/vehicle/sharedbundle_sp_vehicle",
                "win32/gameplay/bundles/sp/sharedbundle_sp",
                "win32/gameplay/bundles/sp/player/sharedbundle_sp_player",
                "win32/gameplay/bundles/sp/droid/sharedbundle_sp_droid",
                "win32/gameplay/bundles/sp/buddy/sharedbundle_sp_buddy",
                "win32/gameplay/bundles/sharedbundles/sp/vehicles/sharedbundlevehicles_sp",
                "win32/gameplay/bundles/sharedbundles/sp/abilities/sharedbundleabilities_sp",
                "win32/a3/gameplay/bundles/sp/vehicle/sharedbundle_sp_vehicle_a3",
                "win32/a3/gameplay/bundles/sp/sharedbundle_sp_a3",
                "win32/a3/gameplay/bundles/sp/player/sharedbundle_sp_player_a3",
                "win32/a3/gameplay/bundles/sp/buddy/sharedbundle_sp_buddy_a3",
                "win32/ui/static",
                "win32/ui/resources/fonts/wsuiimfontconfiguration_languageformat_worstcase",
                "win32/ui/resources/fonts/wsuiimfontconfiguration_languageformat_traditionalchinese",
                "win32/ui/resources/fonts/wsuiimfontconfiguration_languageformat_spanishmex",
                "win32/ui/resources/fonts/wsuiimfontconfiguration_languageformat_spanish",
                "win32/ui/resources/fonts/wsuiimfontconfiguration_languageformat_simplifiedchinese",
                "win32/ui/resources/fonts/wsuiimfontconfiguration_languageformat_russian",
                "win32/ui/resources/fonts/wsuiimfontconfiguration_languageformat_polish",
                "win32/ui/resources/fonts/wsuiimfontconfiguration_languageformat_korean",
                "win32/ui/resources/fonts/wsuiimfontconfiguration_languageformat_japanese",
                "win32/ui/resources/fonts/wsuiimfontconfiguration_languageformat_italian",
                "win32/ui/resources/fonts/wsuiimfontconfiguration_languageformat_german",
                "win32/ui/resources/fonts/wsuiimfontconfiguration_languageformat_french",
                "win32/ui/resources/fonts/wsuiimfontconfiguration_languageformat_english",
                "win32/ui/resources/fonts/wsuiimfontconfiguration_languageformat_brazilianportuguese",
                "win32/ui/resources/fonts/wsuiimfontconfiguration_languageformat_arabicsa",
                "win32/sound/sp/music/screens/sw02_sp_music_loading_bundleasset:8f175ba0-cac3-44bd-87d1-710706c09278",
                "win32/sound/music/loading/sw02_music_loading_bundleasset_initialexperience:c2d5acdb-7a2f-4742-9499-2cd74fefec4c",
                "win32/sound/music/loading/sw02_music_loading_bundleasset:db988158-79d3-489b-96d8-970deca60c67",
                "win32/loadingscreens_bundle",
                "win32/gameplay/bundles/sharedbundles/common/weapons/sharedbundleweapons_common"
        };

        for (String bundle : bundles) {
            this.sharedBundles.put(Fnv1.hashString(bundle.toLowerCase()), bundle);
        }
    }

}
