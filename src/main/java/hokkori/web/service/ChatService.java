package hokkori.web.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hokkori.web.dto.AllianceMemberDto;
import hokkori.web.dto.ChatAttachmentDto;
import hokkori.web.dto.ChatMessageDto;
import hokkori.web.entity.ChannelMaster;
import hokkori.web.entity.ChatAttachment;
import hokkori.web.entity.ChatMessage;
import hokkori.web.entity.ChatMessageView;
import hokkori.web.model.discord.DIscordEventListener;
import hokkori.web.model.member.MemberModel;
import hokkori.web.repository.ChannelMasterRepository;
import hokkori.web.repository.ChatAttachmentRepository;
import hokkori.web.repository.ChatMessageRepository;
import hokkori.web.repository.ChatMessageViewRepository;
import hokkori.web.util.discord.DiscordBot;

@Service
public class ChatService implements DIscordEventListener {

	private static final Logger log = LoggerFactory.getLogger(ChatService.class);
	@Autowired
	private MemberModel memberModel;
	@Autowired
	private DiscordBot discordBot;
	@Autowired
	private ChannelMasterRepository channelRepository;
	@Autowired
	private ChatAttachmentRepository chatAttachmentRepository;
	@Autowired
	private ChatMessageRepository chatMessageRepository;
	@Autowired
	private ChatMessageViewRepository chatMessageViewRepository;
	private ModelMapper modelMapper;

	public ChatService() {
		modelMapper = new ModelMapper();
	}

	@Transactional
	public void init() {
		List<ChannelMaster> channelList = channelRepository.findAll();
		channelList.forEach(channel -> {
			processChannel(channel);
		});
	}

	public boolean existNewMessage(String channelId, int chatMessageid) {
		PageRequest pageable = PageRequest.of(0, 200);
		List<ChatMessageDto> chatMessageDtoList = getChatMessageDtoList(channelId, pageable, null);
		boolean result = false;
		for (ChatMessageDto chatMessageDto : chatMessageDtoList) {
			if (chatMessageDto.getId() > chatMessageid) {
				result = true;
				break;
			}
		}
		return result;
	}

	@Async
	@Transactional // ここでトランザクションを確保
	private void processChannel(ChannelMaster channel) {
		PageRequest pageable = PageRequest.of(0, 200);
		Page<ChatMessage> page = chatMessageRepository.findByChannelMasterId(channel.getId(), pageable);
		List<ChatMessageDto> chatMessageDtoList = modelMapper.map(page.getContent(),
				new TypeToken<List<ChatMessageDto>>() {
				}.getType());
		chatMessageDtoList.forEach(chatMessageDto -> {
			chatMessageDto.setChannelId(channel.getChannelId());
			chatMessageDto.setChannelName(channel.getChannelName());
		});
	}



	@Override
	@Transactional
	public synchronized void onMessageReceived(ChatMessageDto chatMessageDto) {
		ChannelMaster channel = channelRepository.findByChannelId(chatMessageDto.getChannelId());
		String message = chatMessageDto.getMessage().replace("\n", "<br>");
		chatMessageDto.setMessage(message);
		log.info("ChatService:メッセージ:" + chatMessageDto);

		// DB保存
		ChatMessage chatMessage = modelMapper.map(chatMessageDto, ChatMessage.class);
		chatMessage.setChannelMasterId(channel.getId());
		// TODO ChatMessageを保存 配下のChatAttachmentも同時に保存できるはず。うまくいかなかったので暫定
		ChatMessage savedChatMessage = chatMessageRepository.save(chatMessage);
		chatMessageDto.setId(savedChatMessage.getId());

		if (chatMessageDto.getChatAttachmentDtoList().size() != 0) {
			chatMessageDto.getChatAttachmentDtoList().forEach((chatAttachmentDto) -> {
				ChatAttachment chatAttachment = new ChatAttachment();
				chatAttachment.setAttachmentUrl(chatAttachmentDto.getAttachmentUrl());
				chatAttachment.setChatMessage(chatMessage);
				chatAttachment.setAttachmentFileName(chatAttachmentDto.getAttachmentFileName());
				savedChatMessage.getChatAttachmentList().add(chatAttachment);
				chatAttachmentRepository.save(chatAttachment);
			});
		}
	}

	@Transactional
	public ChatMessageDto getChatMessageDto(int id) {
		Optional<ChatMessage> optional = chatMessageRepository.findById(id);
		if (optional.isEmpty())
			return null;
		ChatMessage chatMessage = optional.get();
		ChannelMaster channelMaster = channelRepository.findById(chatMessage.getChannelMasterId()).get();
		List<ChatAttachmentDto> chatAttachmentDtoList = new ArrayList<ChatAttachmentDto>();
		for (ChatAttachment chatAttachment : chatMessage.getChatAttachmentList()) {
			ChatAttachmentDto chatAttachmentDto = new ChatAttachmentDto();
			chatAttachmentDto.setAttachmentFileName(chatAttachment.getAttachmentFileName());
			chatAttachmentDto.setAttachmentUrl(chatAttachment.getAttachmentUrl());
			chatAttachmentDtoList.add(chatAttachmentDto);
		}
		ChatMessageDto chatMessageDto = modelMapper.map(chatMessage, ChatMessageDto.class);
		chatMessageDto.setChannelId(channelMaster.getChannelId());
		chatMessageDto.setChannelName(channelMaster.getChannelName());
		chatMessageDto.setChatAttachmentDtoList(chatAttachmentDtoList);

		return chatMessageDto;
	}

	public void sendMessage(ChatMessageDto chatMessageDto) {
		discordBot.sendMessage(chatMessageDto, memberModel.getAllianceMemberDtoList());
	}

	@Override
	@Transactional
	public void onMessageUpdate(ChatMessageDto chatMessageDto) {
		Optional<ChatMessage> optional = chatMessageRepository.findById(chatMessageDto.getId());
		if (optional.isEmpty())
			return;
		ChatMessage chatMessage = modelMapper.map(chatMessageDto, ChatMessage.class);
		chatMessageRepository.save(chatMessage);
		chatMessageDto.setId(chatMessage.getId());

		if (chatMessageDto.getChatAttachmentDtoList().size() != 0) {
			chatMessageDto.getChatAttachmentDtoList().forEach((chatAttachmentDto) -> {
				ChatAttachment chatAttachment = new ChatAttachment();
				chatAttachment.setAttachmentUrl(chatAttachmentDto.getAttachmentUrl());
				chatAttachment.setChatMessage(chatMessage);
				chatAttachment.setAttachmentFileName(chatAttachmentDto.getAttachmentFileName());
				chatMessage.getChatAttachmentList().add(chatAttachment);
				chatAttachmentRepository.save(chatAttachment);
			});
		}
	}

	//	public List<ChatMessageDto> getChatMessageDtoList(String channelId) {
	//		PageRequest pageable = PageRequest.of(0, 200);
	//		return getChatMessageDtoList(channelId, pageable);
	//	}


	@Transactional
	public List<ChatMessageDto> getChatMessageDtoList(String channelId, Pageable pageable, String ayarabuName) {
		ChannelMaster channel = channelRepository.findByChannelId(channelId);
		List<ChatMessageDto> chatMessageDtoList = new ArrayList<>();
		Page<ChatMessage> chatMessagePage = chatMessageRepository.findByChannelMasterId(channel.getId(), pageable);
		for (ChatMessage chatMessage : chatMessagePage) {
			List<ChatAttachmentDto> chatAttachmentDtoList = new ArrayList<ChatAttachmentDto>();
			for (ChatAttachment chatAttachment : chatMessage.getChatAttachmentList()) {
				ChatAttachmentDto chatAttachmentDto = new ChatAttachmentDto();
				chatAttachmentDto.setAttachmentFileName(chatAttachment.getAttachmentFileName());
				chatAttachmentDto.setAttachmentUrl(chatAttachment.getAttachmentUrl());
				chatAttachmentDtoList.add(chatAttachmentDto);
			}
			ChatMessageDto chatMessageDto = modelMapper.map(chatMessage, ChatMessageDto.class);
			chatMessageDto.setChannelId(channelId);
			chatMessageDto.setChannelName(channel.getChannelName());
			chatMessageDto.setChatAttachmentDtoList(chatAttachmentDtoList);
			chatMessageDtoList.add(chatMessageDto);
		}
		chatMessageDtoList.sort(Comparator.comparing(ChatMessageDto::getId).reversed());
		if (ayarabuName != null && !ayarabuName.isEmpty()) {
			AllianceMemberDto allianceMemberDto = memberModel.getAllianceMemberDtoByAyarabuName(ayarabuName);
			List<ChatMessageView> chatMessageViewList = chatMessageViewRepository
					.findAllByMemberId(allianceMemberDto.getId());
			chatMessageViewList.forEach(chatMessageView -> {
				if (chatMessageView.getChannelId().equals(channelId)) {
					ChatMessageDto firstChatMessageDto = chatMessageDtoList.getFirst();
					chatMessageView.setChatMessageId(firstChatMessageDto.getId());
					chatMessageViewRepository.save(chatMessageView);
				}
			});
		}
		return chatMessageDtoList;
	}

	@Override
	public void onGuildMemberJoin(AllianceMemberDto allianceMemberDto) {
	}

	@Override
	public void onGuildMemberRemove(AllianceMemberDto allianceMemberDto) {
	}

	@Override
	public void onMessageDelete(String messageId) {
		// TODO 自動生成されたメソッド・スタブ

	}

}