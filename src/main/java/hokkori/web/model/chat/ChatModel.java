package hokkori.web.model.chat;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import hokkori.web.HokkoriWebApplication;
import hokkori.web.entity.ChannelMaster;
import hokkori.web.entity.ChatAttachment;
import hokkori.web.entity.ChatMessage;
import hokkori.web.repository.ChatAttachmentRepository;
import hokkori.web.repository.ChatMessageRepository;
import hokkori.web.service.ChatService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

@Component
public class ChatModel {
	private static final Logger log = LoggerFactory.getLogger(ChatService.class);
	@Autowired
	private ChatAttachmentRepository chatAttachmentRepository;
	@Autowired
	private ChatMessageRepository chatMessageRepository;

	private ModelMapper modelMapper;

	public ChatModel() {
		modelMapper = new ModelMapper();
	}
	
	@Transactional // このメソッド全体を単一のトランザクションで実行
	public void saveChatHistory(List<Message> messageList, ChannelMaster channel) {
		List<Message> sortMessageList = new ArrayList<>(messageList);
		sortMessageList.sort(Comparator.comparing(Message::getIdLong));

		for (Message message : sortMessageList) {
			if (chatMessageRepository.findByDiscordMessageId(message.getId()) != null)
				continue;

			// TODO ChannelとChatMessageのリレーションシップを適切に設定する
			// 現在は channel_master_id が使われている

			ChatMessage chatMessage = new ChatMessage();
			ZoneId zone = ZoneId.of("Etc/GMT+9");
			ZonedDateTime zonedDateTime = message.getTimeCreated().atZoneSameInstant(zone);
			Instant instant = zonedDateTime.toInstant();
			Date date = Date.from(instant);
			chatMessage.setCreateDate(HokkoriWebApplication.sdf.format(date));
			chatMessage.setDiscordMessageId(message.getId());
			chatMessage.setMessage(message.getContentDisplay().replace("\n", "<br>"));
			chatMessage.setName(getName(message.getMember()));
			chatMessage.setChannelMasterId(channel.getId()); // この部分はあなたのエンティティ構造に合わせる

			if (message.getReferencedMessage() != null)
				chatMessage.setQuoteDiscordId(message.getReferencedMessage().getId());

			ChatMessage savedChatMessage = chatMessageRepository.save(chatMessage);

			// 添付ファイルの保存
			message.getAttachments().forEach((attachment) -> {
				ChatAttachment chatAttachment = new ChatAttachment();
				chatAttachment.setAttachmentUrl(attachment.getUrl());
				chatAttachment.setChatMessage(savedChatMessage);
				chatAttachment.setAttachmentFileName(attachment.getFileName());
				chatAttachmentRepository.save(chatAttachment);
			});

			// quoteIdの更新
			ChatMessage quoteChatMessage = chatMessageRepository
					.findByDiscordMessageId(savedChatMessage.getQuoteDiscordId());
			if (quoteChatMessage != null) {
				savedChatMessage.setQuoteId(quoteChatMessage.getId().toString());
				chatMessageRepository.save(savedChatMessage);
			}
		}
		log.info("履歴のデータベース保存が完了しました。");
	}

	private String getName(Member member) {
		if (member == null) {
			return "不明";
		}
		String nickname = member.getNickname();
		String effectiveName = member.getEffectiveName();
		if (nickname == null) {
			nickname = effectiveName;
		}
		return nickname;
	}

}
