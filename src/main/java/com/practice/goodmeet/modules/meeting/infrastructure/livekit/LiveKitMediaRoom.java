package com.practice.goodmeet.modules.meeting.infrastructure.livekit;

import com.practice.goodmeet.modules.meeting.application.MediaRoom;
import com.practice.goodmeet.modules.meeting.contract.MediaToken;
import io.livekit.server.AccessToken;
import io.livekit.server.RoomAdmin;
import io.livekit.server.RoomJoin;
import io.livekit.server.RoomName;
import io.livekit.server.RoomRecord;
import io.livekit.server.RoomServiceClient;
import io.livekit.server.WebhookReceiver;
import java.io.IOException;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import livekit.LivekitModels;
import livekit.LivekitWebhook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import retrofit2.Call;
import retrofit2.Response;

@Component
class LiveKitMediaRoom implements MediaRoom {

  private final LiveKitProperties properties;
  private final Clock clock;
  private final RoomServiceClient roomServiceClient;
  private final WebhookReceiver webhookReceiver;

  LiveKitMediaRoom(LiveKitProperties properties, Clock clock) {
    this.properties = properties;
    this.clock = clock;
    this.roomServiceClient =
        RoomServiceClient.createClient(
            toHttpUrl(properties.url()), properties.apiKey(), properties.apiSecret());
    this.webhookReceiver = new WebhookReceiver(properties.apiKey(), properties.apiSecret());
  }

  @Override
  public MediaToken issueToken(
      String roomId, String identity, String displayName, boolean canModerate) {
    AccessToken accessToken = new AccessToken(properties.apiKey(), properties.apiSecret());
    accessToken.setIdentity(identity);
    accessToken.setName(displayName);
    accessToken.setTtl(properties.tokenTtl().toMillis());
    accessToken.getAttributes().put("role", canModerate ? "HOST" : "PARTICIPANT");
    accessToken.addGrants(
        new RoomJoin(true), new RoomName(roomId), new RoomAdmin(canModerate), new RoomRecord(false));
    Instant expiresAt = clock.instant().plus(properties.tokenTtl());
    return new MediaToken(properties.url().toString(), accessToken.toJwt(), expiresAt);
  }

  @Override
  public List<RoomParticipant> listParticipants(String roomId) {
    Response<List<LivekitModels.ParticipantInfo>> response =
        execute(roomServiceClient.listParticipants(roomId));
    if (!response.isSuccessful()) {
      if (response.code() == 404) {
        return List.of();
      }
      throw badGateway("ListParticipants", response.code());
    }
    return response.body().stream().map(LiveKitMediaRoom::toParticipant).toList();
  }

  @Override
  public void removeParticipant(String roomId, String identity) {
    execute(roomServiceClient.removeParticipant(roomId, identity));
  }

  @Override
  public void muteTrack(String roomId, String identity, String trackSid) {
    Response<?> response =
        execute(roomServiceClient.mutePublishedTrack(roomId, identity, trackSid, true));
    if (!response.isSuccessful()) {
      throw badGateway("MutePublishedTrack", response.code());
    }
  }

  @Override
  public void deleteRoom(String roomId) {
    execute(roomServiceClient.deleteRoom(roomId));
  }

  @Override
  public WebhookEvent decodeWebhook(String body, String authorization) {
    LivekitWebhook.WebhookEvent event;
    try {
      event = webhookReceiver.receive(body, authorization);
    } catch (Exception exception) {
      throw new ResponseStatusException(
          HttpStatus.UNAUTHORIZED, "LiveKit webhook verification failed");
    }
    return new WebhookEvent(
        event.getEvent(),
        event.hasRoom() ? event.getRoom().getName() : null,
        event.hasParticipant() ? event.getParticipant().getIdentity() : null);
  }

  private static RoomParticipant toParticipant(LivekitModels.ParticipantInfo info) {
    List<Track> tracks =
        info.getTracksList().stream()
            .map(track -> new Track(track.getSid(), track.getType().name(), track.getMuted()))
            .toList();
    return new RoomParticipant(info.getIdentity(), info.getName(), tracks);
  }

  private static <T> Response<T> execute(Call<T> call) {
    try {
      return call.execute();
    } catch (IOException exception) {
      throw new ResponseStatusException(
          HttpStatus.BAD_GATEWAY, "Could not reach the LiveKit server");
    }
  }

  private static ResponseStatusException badGateway(String operation, int code) {
    return new ResponseStatusException(
        HttpStatus.BAD_GATEWAY, "LiveKit " + operation + " failed with HTTP " + code);
  }

  private static String toHttpUrl(URI url) {
    String value = url.toString();
    if (value.startsWith("wss:")) {
      return "https:" + value.substring(4);
    }
    if (value.startsWith("ws:")) {
      return "http:" + value.substring(3);
    }
    return value;
  }
}
