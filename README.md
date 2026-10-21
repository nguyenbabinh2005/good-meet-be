# Good Meet Backend

Backend cho ứng dụng họp trực tuyến. Spring Boot 3.5, Java 21, PostgreSQL, LiveKit.

## Chạy

```bash
docker compose up -d
mvn spring-boot:run
```

Flyway tự chạy các file trong `src/main/resources/db/migration` khi ứng dụng khởi động.
Biến môi trường tùy chọn: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `LIVEKIT_URL`, `LIVEKIT_API_KEY`, `LIVEKIT_API_SECRET`.

## Xác định người gọi

Không dùng JWT.

- Đăng nhập trả về `userId`. Khi tạo hoặc vào phòng, client gửi `userId` (khách ẩn danh thì bỏ trống) và `displayName` trong body.
- Sau khi vào phòng, client giữ `meetingId`, `publicCode`, `participantId` và gửi `participantId` ở header `X-Participant-Id` cho mọi thao tác trong phòng.
- Token duy nhất là token LiveKit để kết nối phòng media.

## API

| Method | Đường dẫn | Mô tả |
|---|---|---|
| POST | `/auth/register` | Đăng ký, body: `email`, `password`, `displayName`. Trả 201, không có body |
| POST | `/auth/login` | Body: `email`, `password`. Trả `{ userId }` |
| POST | `/meetings` | Tạo meeting ngay, body: `userId`, `displayName`, `title` |
| POST | `/meetings/scheduled` | Đặt lịch, thêm `scheduledStartAt`, `scheduledEndAt` |
| PUT | `/meetings/{code}/schedule` | Sửa lịch (host) |
| POST | `/meetings/{code}/cancel` | Hủy lịch (host) |
| POST | `/meetings/{code}/end-session` | Kết thúc session, đuổi mọi người khỏi phòng (host) |
| POST | `/meetings/{code}/end` | Kết thúc meeting (host) |
| POST, GET | `/meetings/{code}/invitations` | Thêm, xem người được mời (host) |
| DELETE | `/meetings/{code}/invitations/{id}` | Xóa lời mời (host) |
| GET | `/meetings/{code}/room-participants` | Người đang trong phòng LiveKit (host) |
| POST | `/meetings/{code}/room-participants/{identity}/kick` | Kick (host) |
| POST | `/meetings/{code}/room-participants/{identity}/mute` | Mute một track, body: `trackSid` (host) |
| POST | `/meetings/{code}/join` | Vào phòng, body: `userId?`, `displayName` |
| POST | `/join-requests/{requestId}/decision` | Duyệt hoặc từ chối, body: `approved` (host) |
| GET | `/meetings/{code}/chat/messages` | Lịch sử chat của meeting (thành viên) |
| PUT | `/meetings/{code}/hand` | Giơ hoặc hạ tay của chính mình, body: `raised` |
| POST | `/meetings/{code}/hands/{participantId}/lower` | Host hạ tay của participant |
| POST | `/meetings/{code}/reactions` | Gửi reaction, body: `emoji` |
| POST | `/meetings/{code}/breakout-rooms` | Tạo phòng nhỏ, body: `name` (host) |
| PUT | `/meetings/{code}/breakout-rooms/{roomId}/participants/{participantId}` | Chuyển participant sang phòng nhỏ (host) |
| DELETE | `/meetings/{code}/breakout-rooms/{roomId}/participants/{participantId}` | Đưa participant về phòng chính (host hoặc chính participant đó) |
| POST | `/meetings/{code}/breakout-rooms/{roomId}/close` | Đóng phòng nhỏ (host) |
| POST | `/livekit/webhooks` | LiveKit gọi vào |

`join` trả 200 kèm thông tin vào phòng nếu là host. Người khác nhận 202 kèm `{ requestId }`.

## Phòng chờ qua WebSocket

Kết nối STOMP tại `/ws`.

- Host subscribe `/topic/meetings/{meetingId}/waiting-room` để nhận `REQUESTED` và `RESOLVED`.
- Người xin vào subscribe `/topic/join-requests/{requestId}` để nhận `{ approved, result }`. `result` chứa `meetingId`, `publicCode`, `participantId`, `displayName`, `role`, `mediaUrl`, `mediaToken`, `mediaTokenExpiresAt`.

## Chat qua WebSocket

- Gửi tin: STOMP SEND tới `/app/meetings/{code}/chat`, body `{ "participantId": "...", "content": "..." }`. Chỉ gửi được khi meeting có session đang hoạt động.
- Nhận tin: subscribe `/topic/meetings/{meetingId}/chat`, mỗi tin gồm `id`, `meetingId`, `senderParticipantId`, `senderName`, `content`, `sentAt`.

## Giơ tay và reaction qua WebSocket

- Subscribe `/topic/meetings/{meetingId}/hands` để nhận mỗi lần có người giơ hoặc hạ tay: `{ meetingId, participantId, raised, raisedParticipantIds }`. `raisedParticipantIds` là toàn bộ người đang giơ tay, theo thứ tự giơ.
- Subscribe `/topic/meetings/{meetingId}/reactions` để nhận reaction: `{ meetingId, participantId, displayName, emoji }`.
- Trạng thái giơ tay lưu trong bộ nhớ và bị xóa khi session kết thúc.

## Phòng nhỏ qua WebSocket

Mỗi participant subscribe `/topic/participants/{participantId}/room` để biết khi nào phải đổi phòng. Tin nhận được có dạng `{ type, breakoutRoomId, roomName, mediaUrl, mediaToken, mediaTokenExpiresAt }`.

- `type = BREAKOUT`: client ngắt phòng chính và kết nối phòng nhỏ bằng token nhận được.
- `type = MAIN`: client ngắt phòng nhỏ và kết nối lại phòng chính bằng token nhận được. Gửi khi bị đưa về hoặc khi phòng nhỏ đóng.
- Khi session kết thúc, các phòng nhỏ tự đóng và mọi người bị ngắt kết nối.
- Việc ai đang ở phòng nhỏ nào được giữ trong bộ nhớ, mất khi khởi động lại server.

## Cấu trúc

Các module nghiệp vụ nằm trong `modules`, cùng cấp với `shared`. Mỗi module chia `api / application / domain / infrastructure`, và `contract` là phần duy nhất module khác được gọi vào.

| Module | Vai trò |
|---|---|
| `shared` | CORS, STOMP WebSocket, `Clock`, tên header |
| `modules/identity` | Đăng ký, đăng nhập |
| `modules/participant` | Participant và vai trò. Cổng: `ParticipantDirectory` |
| `modules/meeting` | Meeting, phòng chờ, LiveKit. Cổng: `MeetingLookup`, `MeetingSettings`, `MediaRoomAccess`, `MeetingSessionEndedEvent` |
| `modules/chat` | Gửi tin nhắn qua WebSocket, xem lịch sử chat |
| `modules/presence` | Giơ tay, hạ tay, reaction (trong bộ nhớ, không có bảng) |
| `modules/breakout` | Phòng nhỏ: tạo, chuyển người vào và ra, đóng |
