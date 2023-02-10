package moe.caa.multilogin.core.auth;

import moe.caa.multilogin.api.auth.AuthAPI;
import moe.caa.multilogin.api.auth.GameProfile;
import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.core.auth.validate.ValidateAuthenticationResult;
import moe.caa.multilogin.core.auth.validate.ValidateAuthenticationService;
import moe.caa.multilogin.core.auth.yggdrasil.YggdrasilAuthenticationResult;
import moe.caa.multilogin.core.auth.yggdrasil.YggdrasilAuthenticationService;
import moe.caa.multilogin.core.handle.PlayerHandler;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.skinrestorer.SkinRestorerCore;

/**
 * 验证核心
 */
public class AuthHandler implements AuthAPI {
    private final MultiCore core;
    private final YggdrasilAuthenticationService yggdrasilAuthenticationService;
    private final ValidateAuthenticationService validateAuthenticationService;
    private final SkinRestorerCore skinRestorerCore;


    public AuthHandler(MultiCore core) {
        this.core = core;
        this.yggdrasilAuthenticationService = new YggdrasilAuthenticationService(core);
        this.validateAuthenticationService = new ValidateAuthenticationService(core);
        this.skinRestorerCore = new SkinRestorerCore(core);
    }


    /**
     * 开始验证
     *
     * @param username 用户名
     * @param serverId 服务器ID
     * @param ip       用户IP
     */
    @Override
    public LoginAuthResult auth(String username, String serverId, String ip) {
        YggdrasilAuthenticationResult yggdrasilAuthenticationResult;
        try {
            yggdrasilAuthenticationResult = yggdrasilAuthenticationService.hasJoined(username, serverId, ip);
            if (yggdrasilAuthenticationResult.getReason() == YggdrasilAuthenticationResult.Reason.NO_SERVICE) {
                return LoginAuthResult.ofDisallowedByYggdrasilAuthenticator(yggdrasilAuthenticationResult, core.getLanguageHandler().getMessage("auth_yggdrasil_failed_no_server"));
            }
            if (yggdrasilAuthenticationResult.getReason() == YggdrasilAuthenticationResult.Reason.SERVER_BREAKDOWN) {
                return LoginAuthResult.ofDisallowedByYggdrasilAuthenticator(yggdrasilAuthenticationResult, core.getLanguageHandler().getMessage("auth_yggdrasil_failed_server_down"));
            }
            if (yggdrasilAuthenticationResult.getReason() == YggdrasilAuthenticationResult.Reason.VALIDATION_FAILED) {
                return LoginAuthResult.ofDisallowedByYggdrasilAuthenticator(yggdrasilAuthenticationResult, core.getLanguageHandler().getMessage("auth_yggdrasil_failed_validation_failed"));
            }
            if (yggdrasilAuthenticationResult.getReason() != YggdrasilAuthenticationResult.Reason.ALLOWED ||
                    yggdrasilAuthenticationResult.getResponse() == null ||
                    yggdrasilAuthenticationResult.getYggdrasilId() == -1) {
                return LoginAuthResult.ofDisallowedByYggdrasilAuthenticator(yggdrasilAuthenticationResult, core.getLanguageHandler().getMessage("auth_yggdrasil_failed_unknown"));
            }
        } catch (Exception e) {
            LoggerProvider.getLogger().error("An exception occurred while processing the hasJoined request.", e);
            return LoginAuthResult.ofDisallowedByYggdrasilAuthenticator(null, core.getLanguageHandler().getMessage("auth_yggdrasil_error"));
        }

        try {
            ValidateAuthenticationResult validateAuthenticationResult = validateAuthenticationService.checkIn(username, serverId, ip, yggdrasilAuthenticationResult);
            if (validateAuthenticationResult.getReason() == ValidateAuthenticationResult.Reason.ALLOWED) {
                LoggerProvider.getLogger().info(
                        String.format("%s(uuid: %s) from yggdrasil service %s(yid: %d) has been authenticated, profile redirected to %s(uuid: %s).",
                                yggdrasilAuthenticationResult.getResponse().getName(),
                                yggdrasilAuthenticationResult.getResponse().getId().toString(),
                                yggdrasilAuthenticationResult.getYggdrasilServiceConfig().getName(),
                                yggdrasilAuthenticationResult.getYggdrasilId(),
                                validateAuthenticationResult.getInGameProfile().getName(),
                                validateAuthenticationResult.getInGameProfile().getId().toString()
                        )
                );
                GameProfile finalProfile = validateAuthenticationResult.getInGameProfile();
//                GameProfile restoredSkinProfile = null;
//                try {
//                    SkinRestorerResult skinRestorerResult = skinRestorerCore.doRestorer(
//                            yggdrasilAuthenticationResult.getYggdrasilServiceConfig(),
//                            validateAuthenticationResult.getInGameProfile()
//                    );
//                    SkinRestorerResult.handleSkinRestoreResult(skinRestorerResult);
//                    restoredSkinProfile = skinRestorerResult.getResponse();
//                    LoggerProvider.getLogger().debug(String.format("Skin restore result of %s is %s.", yggdrasilAuthenticationResult.getResponse().getName(), skinRestorerResult.getReason()));
//                } catch (Exception e) {
//                    LoggerProvider.getLogger().debug(String.format("Skin restore result of %s is %s.", yggdrasilAuthenticationResult.getResponse().getName(), "error"));
//                    SkinRestorerResult.handleSkinRestoreResult(e);
//                }
//
//                GameProfile finalProfile;
//                if (restoredSkinProfile != null) {
//                    finalProfile = restoredSkinProfile;
//                } else {
//                    finalProfile = validateAuthenticationResult.getInGameProfile();
//                }
                core.getPlayerHandler().getLoginCache().put(finalProfile.getId(), new PlayerHandler.Entry(
                        yggdrasilAuthenticationResult.getResponse(),
                        yggdrasilAuthenticationResult.getYggdrasilId(),
                        System.currentTimeMillis()
                ));
                return LoginAuthResult.ofAllowed(yggdrasilAuthenticationResult, validateAuthenticationResult, finalProfile);
            }
            return LoginAuthResult.ofDisallowedByValidateAuthenticator(yggdrasilAuthenticationResult, validateAuthenticationResult, validateAuthenticationResult.getDisallowedMessage());
        } catch (Exception e) {
            LoggerProvider.getLogger().error("An exception occurred while processing the validation request.", e);
            return LoginAuthResult.ofDisallowedByValidateAuthenticator(yggdrasilAuthenticationResult, null, core.getLanguageHandler().getMessage("auth_validate_error"));
        }
    }
}
