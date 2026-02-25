package gaiden.da.chatservice.dto;

import lombok.Data;

@Data
public class CallSignal {
    private String type;
    private String sdp;
    private Object candidate;
    private Long senderId;
    private Long recipientId;
}
