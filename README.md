# ChessStats

Ứng dụng Android tra cứu hồ sơ và thống kê của người chơi Chess.com bằng tiếng Việt. Nhập username để xem rating, thành tích thắng–hòa–thua và các thể loại cờ mà người chơi đã tham gia. Dữ liệu được lấy từ [Chess.com PubAPI](https://support.chess.com/en/articles/9650547-what-is-the-pubapi-and-how-do-i-use-it); ứng dụng không yêu cầu tài khoản hoặc API key.

## Tính năng

- Tra cứu username Chess.com, hỗ trợ nhập `@` ở đầu và báo lỗi rõ ràng khi tên không hợp lệ hoặc API không phản hồi.
- Hiển thị Rapid, Blitz, Bullet, Daily và các thể loại cờ khác có dữ liệu; chọn một thể loại để xem chi tiết.
- Xem rating hiện tại, rating cao nhất, ngày cập nhật, số ván thắng–hòa–thua và tỉ lệ thắng khi dữ liệu đầy đủ.
- Hiển thị tên, danh hiệu, quốc gia và avatar khi hồ sơ có sẵn. Thống kê vẫn hoạt động nếu hồ sơ hoặc ảnh không tải được.
- Giao diện thích ứng với màn hình dọc, ngang và màn hình rộng; giữ kết quả khi xoay thiết bị.

## Yêu cầu

| Thành phần | Phiên bản / điều kiện |
| --- | --- |
| Android Studio | Android SDK Platform 37; bản build đã được kiểm tra với JDK 21 đi kèm Android Studio |
| Thiết bị hoặc emulator | Android 7.0 (API 24) trở lên |
| Kết nối mạng | Cần để tải dữ liệu công khai từ Chess.com |

Dự án dùng Java 11, Android Views/XML, Gradle 9.6 và Android Gradle Plugin 9.3. Các phiên bản SDK và plugin được khai báo trong file Gradle của dự án.

## Cài đặt và chạy

1. Clone repository:

   ```powershell
   git clone https://github.com/Dung092005/Mobile-project.git
   cd Mobile-project
   ```

2. Mở thư mục `Mobile-project` trong Android Studio và chờ Gradle Sync hoàn tất. Nếu IDE yêu cầu, cài Android SDK Platform 37 và chọn JDK đi kèm Android Studio cho Gradle.
3. Chọn thiết bị hoặc emulator Android API 24 trở lên, rồi bấm **Run app**.
4. Trong ứng dụng, nhập username như `hikaru` hoặc `@hikaru`, bấm **Tra cứu** và chọn thể loại cờ muốn xem.

### Kiểm thử và tạo APK trên Windows

Trong PowerShell, đặt `JAVA_HOME` và `ANDROID_HOME` theo vị trí cài đặt của bạn. Với đường dẫn Android Studio mặc định:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
$env:ANDROID_HOME = Join-Path $env:LOCALAPPDATA 'Android\Sdk'
.\gradlew.bat testDebugUnitTest :app:assembleDebug
```

APK debug được tạo tại `app/build/outputs/apk/debug/app-debug.apk`.

## Cấu trúc dự án

| Phần | Vai trò |
| --- | --- |
| `app/src/main/java/com/chess/stats/MainActivity.java` | Điều khiển tra cứu, màn hình kết quả và trạng thái khi xoay thiết bị |
| `app/src/main/java/com/chess/stats/data/` | Gọi API, xử lý lỗi, đọc JSON và tạo model thống kê |
| `app/src/main/java/com/chess/stats/ui/` | Hiển thị hồ sơ, các thể loại cờ và kết quả |
| `app/src/main/res/` | Layout dọc/ngang, chuỗi tiếng Việt, màu, kiểu chữ và icon |
| `app/src/test/` | Unit test cho parser, username, mạng và các helper |

Ứng dụng đọc hai endpoint công khai: `/pub/player/{username}/stats` cho thống kê và `/pub/player/{username}` cho hồ sơ. Dữ liệu thống kê là bắt buộc để hiện kết quả; hồ sơ là dữ liệu bổ sung.

## Thành viên

| Thành viên | Phần việc |
| --- | --- |
| [Dung092005](https://github.com/Dung092005) | Tích hợp dự án, cấu hình build, giao diện nền tảng, tài liệu |
| [leducanh21122003](https://github.com/leducanh21122003) | Activity, điều hướng và bố cục thích ứng |
| [bin1310](https://github.com/bin1310) | Màn hình tra cứu và thiết kế giao diện chung |
| [duchla2005](https://github.com/duchla2005) | Hồ sơ, avatar và thông tin quốc gia |
| [hieunm23ba14105-star](https://github.com/hieunm23ba14105-star) | API, xử lý mạng, lỗi và luồng tra cứu |
| [minmiwn](https://github.com/minmiwn) | Thống kê, các thể loại cờ và màn hình kết quả |

## Giấy phép

Dự án được phát hành theo [MIT License](LICENSE). Copyright © 2026 ChessStats contributors.
