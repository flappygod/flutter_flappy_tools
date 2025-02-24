import Flutter
import UIKit
import AudioToolbox
import AVFoundation
import Photos

public class FlutterFlappyToolsPlugin: NSObject, FlutterPlugin {
    // 注册插件并创建方法通道
    public static func register(with registrar: FlutterPluginRegistrar) {
        // 创建方法通道
        let channel = FlutterMethodChannel(name: "flutter_flappy_tools", binaryMessenger: registrar.messenger())
        let instance = FlutterFlappyToolsPlugin()
        // 将插件实例注册为方法通道的委托
        registrar.addMethodCallDelegate(instance, channel: channel)
    }
    
    // 处理来自 Flutter 的方法调用
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        switch call.method {
        case "getPathSize":
            // 获取路径大小
            if let args = call.arguments as? [String: Any],
               let path = args["path"] as? String,
               let type = args["type"] as? Int {
                let size = calculateSize(path: path)
                let formattedSize = formatSize(size: size, type: type)
                result(formattedSize)
            } else {
                // 参数错误时返回错误
                result(FlutterError(code: "INVALID_ARGUMENTS", message: "Invalid arguments for getPathSize", details: nil))
            }
        case "clearPath":
            // 清除指定路径的文件
            if let args = call.arguments as? [String: Any],
               let path = args["path"] as? String {
                clearPath(path: path)
                result("1")
            } else {
                result(FlutterError(code: "INVALID_ARGUMENTS", message: "Invalid arguments for clearPath", details: nil))
            }
        case "getBrightness":
            // 获取屏幕亮度
            let brightness = UIScreen.main.brightness
            result(String(format: "%.2f", brightness))
        case "getDeviceLocal":
            // 获取设备的当前语言设置
            let language = Locale.preferredLanguages.first ?? "en-US"
            result(language)
        case "setBrightness":
            // 设置屏幕亮度
            if let args = call.arguments as? [String: Any],
               let brightness = args["brightness"] as? String,
               let value = Double(brightness) {
                UIScreen.main.brightness = CGFloat(value)
                result("1")
            } else {
                result(FlutterError(code: "INVALID_ARGUMENTS", message: "Invalid arguments for setBrightness", details: nil))
            }
        case "getBatteryLevel":
            // 获取设备电池电量
            UIDevice.current.isBatteryMonitoringEnabled = true
            let batteryLevel = UIDevice.current.batteryLevel
            result(String(format: "%.2f", batteryLevel))
        case "getBatteryCharge":
            // 检查设备是否正在充电
            UIDevice.current.isBatteryMonitoringEnabled = true
            let isCharging = UIDevice.current.batteryState == .charging
            result(isCharging ? "1" : "0")
        case "setScreenSteadyLight":
            // 设置屏幕常亮状态
            if let args = call.arguments as? [String: Any],
               let state = args["state"] as? String {
                UIApplication.shared.isIdleTimerDisabled = (state == "1")
                result("1")
            } else {
                result(FlutterError(code: "INVALID_ARGUMENTS", message: "Invalid arguments for setScreenSteadyLight", details: nil))
            }
        case "shake":
            // 触发设备震动
            AudioServicesPlaySystemSound(kSystemSoundID_Vibrate)
            result("1")
        case "addManagerCookie":
            // 添加 Cookie
            if let args = call.arguments as? [String: Any],
               let cookie = args["cookie"] as? String,
               let url = args["url"] as? String {
                addCookie(cookie: cookie, url: url)
                result("1")
            } else {
                result(FlutterError(code: "INVALID_ARGUMENTS", message: "Invalid arguments for addManagerCookie", details: nil))
            }
        case "share":
            // 调用系统分享功能
            if let args = call.arguments as? [String: Any],
               let text : String = args["share"] as? String {
                shareText(text: text, result: result)
            } else {
                result(FlutterError(code: "INVALID_ARGUMENTS", message: "Invalid arguments for share", details: nil))
            }
        case "jumpToUrl", "jumpToScheme":
            // 打开指定的 URL 或 Scheme
            if let args = call.arguments as? [String: Any],
               let url = args["url"] as? String {
                openScheme(url: url)
                result("true")
            } else {
                result(FlutterError(code: "INVALID_ARGUMENTS", message: "Invalid arguments for jumpToUrl/jumpToScheme", details: nil))
            }
        default:
            // 未实现的方法
            result(FlutterMethodNotImplemented)
        }
    }
    
    // MARK: - Helper Methods
    
    // 计算路径的大小（包括文件和文件夹）
    private func calculateSize(path: String) -> Int64 {
        let fileManager = FileManager.default
        var isDirectory: ObjCBool = false
        if fileManager.fileExists(atPath: path, isDirectory: &isDirectory) {
            if isDirectory.boolValue {
                return folderSize(atPath: path)
            } else {
                return fileSize(atPath: path)
            }
        }
        return 0
    }
    
    // 获取文件的大小
    private func fileSize(atPath path: String) -> Int64 {
        let fileManager = FileManager.default
        if let attributes = try? fileManager.attributesOfItem(atPath: path),
           let fileSize = attributes[.size] as? Int64 {
            return fileSize
        }
        return 0
    }
    
    // 获取文件夹的大小
    private func folderSize(atPath path: String) -> Int64 {
        let fileManager = FileManager.default
        guard let enumerator = fileManager.enumerator(atPath: path) else { return 0 }
        var totalSize: Int64 = 0
        for case let file as String in enumerator {
            let filePath = (path as NSString).appendingPathComponent(file)
            totalSize += fileSize(atPath: filePath)
        }
        return totalSize
    }
    
    // 格式化文件大小为指定单位
    private func formatSize(size: Int64, type: Int) -> String {
        switch type {
        case 1: return String(format: "%.2f", Double(size))
        case 2: return String(format: "%.2f", Double(size) / 1024)
        case 3: return String(format: "%.2f", Double(size) / 1048576)
        case 4: return String(format: "%.2f", Double(size) / 1073741824)
        default: return "0"
        }
    }
    
    // 清除指定路径的文件
    private func clearPath(path: String) {
        let fileManager = FileManager.default
        try? fileManager.removeItem(atPath: path)
    }
    
    // 添加 Cookie 到指定的 URL
    private func addCookie(cookie: String, url: String) {
        let components = cookie.split(separator: "=")
        guard components.count == 2 else { return }
        let name = String(components[0])
        let value = String(components[1])
        let properties: [HTTPCookiePropertyKey: Any] = [
            .name: name,
            .value: value,
            .domain: URL(string: url)?.host ?? "",
            .path: "/",
            .version: "0",
            .expires: Date(timeIntervalSinceNow: 3600 * 24 * 30 * 12)
        ]
        if let cookie = HTTPCookie(properties: properties) {
            HTTPCookieStorage.shared.setCookie(cookie)
        }
    }
    
    // 调用系统分享功能
    private func shareText(text: String, result: @escaping FlutterResult) {
        let activityVC = UIActivityViewController(activityItems: [text], applicationActivities: nil)
        if let topController = UIApplication.shared.keyWindow?.rootViewController {
            topController.present(activityVC, animated: true, completion: nil)
        }
        result("true")
    }
    
    // 打开指定的 URL 或 Scheme
    private func openScheme(url: String) {
        if let encodedURL = url.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed),
           let url = URL(string: encodedURL) {
            UIApplication.shared.open(url, options: [:], completionHandler: nil)
        }
    }
}
