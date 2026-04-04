package com.qituo.dcc.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mojang API 皮肤获取工具类
 * 用于从Mojang服务器获取玩家皮肤信息
 */
public class MojangSkinAPI {
    
    private static final String UUID_API_URL = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String PROFILE_API_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";
    private static final int MAX_RETRIES = 3;
    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 5000;
    private static final int RETRY_DELAY = 1000;
    
    private static final Map<String, CachedSkin> SKIN_CACHE = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION = 24 * 60 * 60 * 1000;
    
    private static class CachedSkin {
        final GameProfile profile;
        final long timestamp;
        
        CachedSkin(GameProfile profile) {
            this.profile = profile;
            this.timestamp = System.currentTimeMillis();
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_DURATION;
        }
    }
    
    /**
     * 从Mojang API获取玩家皮肤信息
     * @param username 玩家名称
     * @return 包含皮肤信息的GameProfile，如果获取失败则返回null
     */
    public static GameProfile fetchSkinFromMojang(String username) {
        if (username == null || username.isEmpty()) {
            return null;
        }
        
        String lowerUsername = username.toLowerCase();
        
        CachedSkin cached = SKIN_CACHE.get(lowerUsername);
        if (cached != null && !cached.isExpired()) {
            System.out.println("[MojangSkinAPI] 使用缓存的皮肤信息: " + username);
            return cached.profile;
        }
        
        try {
            System.out.println("[MojangSkinAPI] 从Mojang API获取皮肤信息: " + username);
            
            String uuid = fetchUUID(username);
            if (uuid == null) {
                System.out.println("[MojangSkinAPI] 无法获取玩家UUID: " + username);
                return null;
            }
            
            GameProfile profile = fetchProfile(uuid);
            if (profile != null) {
                SKIN_CACHE.put(lowerUsername, new CachedSkin(profile));
                System.out.println("[MojangSkinAPI] 皮肤信息已缓存: " + username);
            }
            
            return profile;
        } catch (Exception e) {
            ExceptionHandler.handleNetworkException("获取皮肤信息", e);
            return null;
        }
    }
    
    /**
     * 清除皮肤缓存
     */
    public static void clearCache() {
        SKIN_CACHE.clear();
        System.out.println("[MojangSkinAPI] 皮肤缓存已清除");
    }
    
    private static String makeRequest(String urlString) throws IOException, InterruptedException {
        int attempts = 0;
        IOException lastException = null;
        
        while (attempts < MAX_RETRIES) {
            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(CONNECT_TIMEOUT);
                connection.setReadTimeout(READ_TIMEOUT);
                
                int responseCode = connection.getResponseCode();
                
                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
                    );
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    connection.disconnect();
                    return response.toString();
                } else if (responseCode == 204) {
                    connection.disconnect();
                    return null;
                } else {
                    System.out.println("[MojangSkinAPI] 请求失败，响应码: " + responseCode);
                    connection.disconnect();
                }
            } catch (IOException e) {
                lastException = e;
                System.out.println("[MojangSkinAPI] 请求异常，尝试 " + (attempts + 1) + "/" + MAX_RETRIES + ": " + e.getMessage());
            }
            
            attempts++;
            if (attempts < MAX_RETRIES) {
                Thread.sleep(RETRY_DELAY);
            }
        }
        
        if (lastException != null) {
            throw lastException;
        }
        return null;
    }
    
    /**
     * 从Mojang API获取玩家UUID
     * @param username 玩家名称
     * @return 玩家UUID字符串，如果获取失败则返回null
     */
    private static String fetchUUID(String username) {
        try {
            String response = makeRequest(UUID_API_URL + username);
            if (response == null) {
                System.out.println("[MojangSkinAPI] 玩家不存在: " + username);
                return null;
            }
            
            JsonObject json = JsonParser.parseString(response).getAsJsonObject();
            if (json.has("id")) {
                return json.get("id").getAsString();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("[MojangSkinAPI] 请求被中断");
        } catch (Exception e) {
            ExceptionHandler.handleNetworkException("获取皮肤数据", e);
        }
        return null;
    }
    
    /**
     * 从Mojang API获取玩家档案（包含皮肤信息）
     * @param uuid 玩家UUID（不带连字符的格式）
     * @return 包含皮肤信息的GameProfile，如果获取失败则返回null
     */
    private static GameProfile fetchProfile(String uuid) {
        try {
            String response = makeRequest(PROFILE_API_URL + uuid + "?unsigned=false");
            if (response == null) {
                return null;
            }
            
            JsonObject json = JsonParser.parseString(response).getAsJsonObject();
            String name = json.has("name") ? json.get("name").getAsString() : "Unknown";
            
            UUID playerUUID = fromUndashedUUID(uuid);
            GameProfile profile = new GameProfile(playerUUID, name);
            
            if (json.has("properties")) {
                JsonArray properties = json.getAsJsonArray("properties");
                for (JsonElement element : properties) {
                    JsonObject property = element.getAsJsonObject();
                    String propName = property.get("name").getAsString();
                    String propValue = property.get("value").getAsString();
                    String propSignature = property.has("signature") ? property.get("signature").getAsString() : null;
                    
                    profile.getProperties().put(propName, new Property(propName, propValue, propSignature));
                    System.out.println("[MojangSkinAPI] 成功获取皮肤属性: " + propName);
                }
            }
            
            return profile;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("[MojangSkinAPI] 请求被中断");
        } catch (Exception e) {
            ExceptionHandler.handleNetworkException("获取皮肤数据", e);
        }
        return null;
    }
    
    /**
     * 将不带连字符的UUID字符串转换为UUID对象
     * @param undashedUUID 不带连字符的UUID字符串
     * @return UUID对象
     */
    private static UUID fromUndashedUUID(String undashedUUID) {
        if (undashedUUID.length() != 32) {
            return UUID.randomUUID();
        }
        return UUID.fromString(
            undashedUUID.substring(0, 8) + "-" +
            undashedUUID.substring(8, 12) + "-" +
            undashedUUID.substring(12, 16) + "-" +
            undashedUUID.substring(16, 20) + "-" +
            undashedUUID.substring(20, 32)
        );
    }
    
    /**
     * 解析皮肤纹理URL
     * @param profile GameProfile对象
     * @return 皮肤URL，如果没有则返回null
     */
    public static String getSkinUrl(GameProfile profile) {
        try {
            if (profile.getProperties().containsKey("textures")) {
                Property texturesProperty = profile.getProperties().get("textures").iterator().next();
                String texturesValue = texturesProperty.getValue();
                
                // Base64解码
                String decoded = new String(Base64.getDecoder().decode(texturesValue), StandardCharsets.UTF_8);
                JsonObject texturesJson = JsonParser.parseString(decoded).getAsJsonObject();
                
                if (texturesJson.has("textures") && 
                    texturesJson.getAsJsonObject("textures").has("SKIN")) {
                    return texturesJson.getAsJsonObject("textures")
                        .getAsJsonObject("SKIN")
                        .get("url")
                        .getAsString();
                }
            }
        } catch (Exception e) {
            ExceptionHandler.handleJsonException("解析皮肤URL", e);
        }
        return null;
    }
    
    /**
     * 解析披风纹理URL
     * @param profile GameProfile对象
     * @return 披风URL，如果没有则返回null
     */
    public static String getCapeUrl(GameProfile profile) {
        try {
            if (profile.getProperties().containsKey("textures")) {
                Property texturesProperty = profile.getProperties().get("textures").iterator().next();
                String texturesValue = texturesProperty.getValue();
                
                // Base64解码
                String decoded = new String(Base64.getDecoder().decode(texturesValue), StandardCharsets.UTF_8);
                JsonObject texturesJson = JsonParser.parseString(decoded).getAsJsonObject();
                
                if (texturesJson.has("textures") && 
                    texturesJson.getAsJsonObject("textures").has("CAPE")) {
                    return texturesJson.getAsJsonObject("textures")
                        .getAsJsonObject("CAPE")
                        .get("url")
                        .getAsString();
                }
            }
        } catch (Exception e) {
            ExceptionHandler.handleJsonException("解析披风URL", e);
        }
        return null;
    }
}
