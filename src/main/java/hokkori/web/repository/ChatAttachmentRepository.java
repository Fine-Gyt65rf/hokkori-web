package hokkori.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import hokkori.web.entity.ChatAttachment;

@Repository
public interface ChatAttachmentRepository extends JpaRepository<ChatAttachment, Integer>{
}
