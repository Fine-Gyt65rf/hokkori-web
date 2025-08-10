package hokkori.web.dto;

import java.io.InputStream;

import lombok.Data;

@Data
public class ChatAttachmentDto {
	private String attachmentUrl;
	private String attachmentFileName;
	private InputStream attachmentFileInputStream;
}
