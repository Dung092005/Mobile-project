# ChessStats Project Main — Tài liệu thiết kế

## Mục tiêu

Xây dựng ứng dụng Android native bằng Java để tra cứu và hiển thị thống kê người chơi Chess.com. Ứng dụng chỉ xem dữ liệu thống kê; không có chức năng chơi cờ hoặc đăng nhập tài khoản Chess.com.

## Công nghệ

- Java 11 cho mã nguồn ứng dụng.
- Android Views và XML cho giao diện.
- Android SDK: min SDK 24, target SDK 37, compile SDK 37.
- Gọi Chess.com Public API bằng HttpURLConnection và xử lý JSON bằng org.json.
- Dùng ExecutorService để chạy request mạng và tải avatar ngoài UI thread.
- Không dùng WebView, Kotlin hoặc Jetpack Compose trong runtime của app.
- Application ID: com.chess.stats.

## Trải nghiệm người dùng

Màn hình tra cứu mặc định bằng tiếng Việt. Người dùng nhập username Chess.com, có thể nhập dấu @ ở đầu. Username được làm sạch và kiểm tra trước khi gọi API. Nút Tra cứu hiển thị trạng thái đang tải và bị khóa trong lúc request. Khi lỗi, username vẫn được giữ lại để người dùng sửa và thử lại.

Sau khi tra cứu thành công, màn hình hiển thị thanh Chess.com — Thống kê, thẻ thông tin người chơi, avatar, danh hiệu, tên, username, quốc gia và số thể loại cờ có dữ liệu. Người dùng có thể bấm Đổi người chơi để quay lại form.

Các ô thể loại gồm Cờ nhanh, Cờ chớp, Cờ siêu chớp, Cờ hàng ngày và các thể loại khác nếu API trả về. Khi chọn một ô, ứng dụng hiển thị điểm hiện tại, ngày cập nhật, điểm cao nhất, ngày đạt điểm cao nhất, liên kết ván đạt mốc nếu có, số trận thắng/hòa/thua, thanh tỷ lệ, tổng số ván và tỷ lệ thắng khi đủ dữ liệu.

Ở chế độ dọc, thanh thương hiệu và thẻ người chơi nằm phía trên, phần thống kê cuộn bên dưới. Ở chế độ ngang, phần thương hiệu/người chơi nằm ở pane trái và thống kê ở pane phải. Trên màn hình rộng, lưới thể loại có thể dùng ba cột.

## Luồng dữ liệu

1. MainActivity nhận username.
2. UsernameNormalizer làm sạch và kiểm tra username.
3. ChessComRepository chạy lookup ở worker thread.
4. PlayerLookup gọi endpoint stats bắt buộc: https://api.chess.com/pub/player/{username}/stats.
5. StatsParser lọc các key chess, bỏ các block như tactics, lessons, puzzle_rush và fide, sau đó sắp xếp mode.
6. PlayerLookup gọi profile ở endpoint https://api.chess.com/pub/player/{username}. Profile là dữ liệu bổ sung; nếu profile lỗi thì stats hợp lệ vẫn được hiển thị.
7. Callback về main thread để MainActivity bind dữ liệu vào ProfileCard và StatsPanel.
8. Raw JSON được lưu trong state để khôi phục kết quả sau khi xoay màn hình mà không cần gọi API lại.

## Cấu trúc mã nguồn

- MainActivity.java: quản lý trạng thái LANDING, LOADING, RESULTS, sự kiện tìm kiếm, back navigation, lưu state và edge-to-edge.
- ChessComRepository.java: quản lý executor, callback, cache avatar và hủy request avatar cũ.
- PlayerLookup.java: điều phối request profile và stats.
- HttpUrlConnectionFetcher.java: gọi HTTP, timeout, giới hạn response, User-Agent và ánh xạ lỗi.
- StatsParser.java: parse JSON stats thành object thống kê.
- PlayerProfile.java, PlayerStats.java, GameModeStats.java, LookupResult.java: các model dữ liệu.
- ProfileCard.java: hiển thị thông tin người chơi.
- StatsPanel.java: tạo ô mode và bind thẻ tổng quan/kết quả.
- ModeLabels.java: đổi key API thành tên hiển thị và chọn icon.
- Formatters.java: format rating, số lượng và ngày.
- CountryHelper.java: đổi country URL thành mã, tên tiếng Việt và cờ emoji.
- ErrorMessages.java: đổi lỗi thành thông báo tiếng Việt.
- EdgeToEdge.java: xử lý system bars, bàn phím và inset.

## Xử lý lỗi

Ứng dụng xử lý username rỗng, username sai định dạng, HTTP 404/410, HTTP 429, lỗi máy chủ 5xx, timeout, lỗi mạng, JSON sai, không có thống kê, profile lỗi, avatar lỗi và trường hợp thiết bị không có trình duyệt để mở link. Profile và avatar là dữ liệu phụ; nếu lỗi, ứng dụng vẫn cố gắng hiển thị stats.

## Tài nguyên giao diện

- activity_main.xml và layout-land/activity_main.xml: layout dọc/ngang.
- view_landing.xml, view_search_card.xml: màn hình tra cứu.
- view_brand_bar.xml, view_profile_card.xml: phần thương hiệu và người chơi.
- view_stats_content.xml, item_mode_tile.xml: phần thống kê và các ô thể loại.
- res/values: màu, kích thước, style, text và cấu hình lưới.
- tile_min_height điều chỉnh chiều cao tối thiểu của mỗi ô.
- mode_grid_columns điều chỉnh số ô trên một hàng: 2 ở màn hình thường và 3 trên màn hình rộng.

## Giới hạn hiện tại

- Phụ thuộc Internet và Chess.com Public API.
- Không có database, đăng nhập, lưu username lâu dài hoặc lịch sử tìm kiếm.
- Chỉ hiển thị những trường API trả về; trường thiếu được hiển thị là chưa có dữ liệu.
- Stats là dữ liệu bắt buộc; profile là dữ liệu bổ sung.
- Project main hiện dùng bộ resource tiếng Việt, chưa có thư mục values-en.

## Kiểm thử

Project có unit test cho parser stats, profile parser, chuẩn hóa username, ánh xạ lỗi HTTP, giới hạn response, khôi phục state, country code và tên mode. APK debug nằm tại app/build/outputs/apk/debug/app-debug.apk.

