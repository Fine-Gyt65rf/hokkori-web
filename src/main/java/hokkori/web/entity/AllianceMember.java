package hokkori.web.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import jp.highwide.common.excel.ExcelColumn;
import jp.highwide.common.excel.ExcelEntity;
import jp.highwide.common.excel.ExcelEntity.Align;
import jp.highwide.common.excel.ExcelEntity.Border;
import lombok.Data;

@Data
@Entity
@ExcelEntity(header = true, headerStyle = Align.ALIGN_CENTER, borderStyle = Border.STYLE2)
public class AllianceMember {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@ExcelColumn(columnIndex = 0)
	private Integer id;
	@ExcelColumn(columnIndex = 1)
	private String memberRole;
	@ExcelColumn(columnIndex = 2)
	private String discordMemberId;
	@ExcelColumn(columnIndex = 3)
	private String discordName;
	@ExcelColumn(columnIndex = 4)
	private String ayarabuId;
	@ExcelColumn(columnIndex = 5)
	private String ayarabuName;
	@ExcelColumn(columnIndex = 6)
	private String alliance;
	@ExcelColumn(columnIndex = 7)
	private Integer statementCount;
	@ExcelColumn(columnIndex = 8)
	private String createDate;
	@ExcelColumn(columnIndex = 9)
	private Integer bot;
}
