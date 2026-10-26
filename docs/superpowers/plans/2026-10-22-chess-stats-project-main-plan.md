# ChessStats Project Main — Kế hoạch triển khai

> Tài liệu này mô tả cấu trúc triển khai hiện tại của Project main và các bước kiểm tra khi tiếp tục phát triển.

## Mục tiêu

Duy trì ứng dụng Android Java/XML tra cứu thống kê người chơi Chess.com với luồng một màn hình, xử lý mạng bất đồng bộ, giao diện responsive và dữ liệu tiếng Việt.

## Kiến trúc

- Một MainActivity quản lý màn hình tra cứu và màn hình kết quả.
- Lớp data gọi API, parse JSON và chuyển lỗi thành các kiểu rõ ràng.
- Lớp UI bind model vào XML views.
- Resource XML quản lý màu, kích thước, text, icon và layout theo hướng dọc/ngang/màn hình rộng.
- Networking dùng HttpURLConnection và ExecutorService; request không chạy trên main thread.

## Các phần đã có

### Màn hình tra cứu

- Form nhập username bằng XML.
- Chuẩn hóa username có hỗ trợ dấu @.
- Kiểm tra input trước khi gọi mạng.
- Loading state và giữ input khi lỗi.

### Lớp dữ liệu và API

- Fetch stats bắt buộc và profile tùy chọn.
- Parse rating, ngày, best game URL và record thắng/hòa/thua.
- Chỉ chấp nhận URL HTTPS; link ván cao nhất phải thuộc chess.com hoặc subdomain của chess.com.
- Có giới hạn kích thước response.
- Đóng stream và disconnect HTTP connection.

### Màn hình kết quả

- Profile card với avatar fallback.
- Quốc gia hiển thị bằng tên tiếng Việt và cờ emoji khi country URL hợp lệ.
- Mode cards được tạo động từ dữ liệu API.
- Mode phổ biến hiển thị trước; mode khác nằm trong vùng mở rộng.
- Thẻ tổng quan và thẻ kết quả cập nhật khi chọn mode.

### Khả năng thích ứng màn hình

- Portrait: header/profile cố định, stats cuộn bên dưới.
- Landscape: header/profile ở pane trái, stats ở pane phải.
- Màn hình rộng: lưới mode có ba cột.
- Edge-to-edge và bàn phím được xử lý bằng EdgeToEdge.

### Khôi phục trạng thái

- Lưu username, mode đang chọn, raw stats/profile JSON và trạng thái lỗi.
- Sau rotation, parse lại raw JSON để khôi phục giao diện.
- Avatar được cache trong process để tránh tải lại không cần thiết.

## Các bước kiểm tra

- [x] Manifest có quyền Internet và launcher MainActivity.
- [x] XML resource parse được.
- [x] Unit test cho data, parser, username, country và mode label.
- [x] APK debug đã được tạo.
- [ ] Chạy app trên emulator để kiểm tra portrait, landscape và nhập username.
- [ ] Kiểm tra lỗi 404, 429, timeout, mất mạng và profile không tải được.
- [ ] Kiểm tra xoay màn hình khi đang ở màn hình kết quả.
- [ ] Kiểm tra mode thiếu rating hoặc thiếu record.

## Rủi ro và cách xử lý

- API thay đổi field: parser giữ giá trị thiếu là null và UI hiển thị Chưa có dữ liệu.
- Profile lỗi nhưng stats hợp lệ: không chặn kết quả; dùng thông tin mặc định.
- Avatar chậm/lỗi: dùng cache hoặc icon mặc định.
- Thiết bị nhỏ hoặc font lớn: dùng ScrollView và layout responsive.
- Link ngoài không có trình duyệt: bắt ActivityNotFoundException và hiển thị thông báo.

## Không thuộc phạm vi

- Không có chức năng chơi cờ.
- Không có đăng nhập Chess.com.
- Không có database hoặc đồng bộ offline.
- Không thêm video nền, practice section hoặc PubAPI footer riêng.
- Không đổi sang Kotlin/Compose.

## Ghi chú khi thuyết trình

Có thể giải thích theo chuỗi:

1. MainActivity nhận username và điều khiển giao diện.
2. ChessComRepository chạy request ở background.
3. PlayerLookup gọi profile/stats API.
4. StatsParser chuyển JSON thành model Java.
5. ProfileCard và StatsPanel hiển thị dữ liệu.
6. Error type và fallback giúp app không crash khi mạng/API thiếu dữ liệu.

