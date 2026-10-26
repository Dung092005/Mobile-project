# ChessStats

Ứng dụng Android viết bằng Java và XML để tra cứu hồ sơ, rating và kết quả thi đấu của người chơi Chess.com qua Public API.

## Chức năng

- Tra cứu bằng username Chess.com, có thể nhập dấu `@` ở đầu.
- Xem các thể loại cờ, rating hiện tại và cao nhất, số ván thắng, hòa, thua.
- Xem hồ sơ, danh hiệu, quốc gia và ảnh đại diện khi API cung cấp dữ liệu.
- Giao diện tiếng Việt cho màn hình dọc, ngang và thiết bị màn hình rộng.

Mở thư mục này bằng Android Studio để chạy ứng dụng. Có thể chạy kiểm thử JVM và tạo APK debug bằng `gradlew.bat testDebugUnitTest :app:assembleDebug` trên Windows.

## Thành viên và phần việc được ghi nhận

| Thành viên | Phần việc |
| --- | --- |
| [Dung092005](https://github.com/Dung092005) | Tích hợp dự án, cấu hình build, giao diện nền tảng, tài liệu |
| [leducanh21122003](https://github.com/leducanh21122003) | Activity, điều hướng và bố cục thích ứng |
| [bin1310](https://github.com/bin1310) | Màn hình tra cứu và thiết kế giao diện chung |
| [duchla2005](https://github.com/duchla2005) | Hồ sơ, avatar và thông tin quốc gia |
| [hieunm23ba14105-star](https://github.com/hieunm23ba14105-star) | API, xử lý mạng, lỗi và luồng tra cứu |
| [minmiwn](https://github.com/minmiwn) | Thống kê, các thể loại cờ và màn hình kết quả |

## Ghi chú về lịch sử Git

Nhóm đã hoàn thành mã nguồn trong dự án ChessStats trước khi đưa sang repository này. Khi nhập dự án, nhóm trưởng tái dựng lịch sử thành 40 commit theo phần việc đã được cả nhóm thống nhất. Tên trong metadata commit ghi nhận đóng góp theo phân công; quá trình tạo và đẩy commit được thực hiện tập trung vào ngày 26/10/2026.
