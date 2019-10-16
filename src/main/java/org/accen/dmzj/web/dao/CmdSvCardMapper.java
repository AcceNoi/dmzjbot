package org.accen.dmzj.web.dao;

import java.util.List;

import org.accen.dmzj.web.vo.CmdSvCard;
import org.accen.dmzj.web.vo.CmdSvPk;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CmdSvCardMapper {
	/**
	 * 查询一个卡包
	 * @param id
	 * @return
	 */
	@Results(id = "cmdSvPkMapper",value = {
			@Result(property = "id",column = "id"),
			@Result(property = "pkName",column = "pk_name"),
			@Result(property = "pkAlias",column = "pk_alias"),
			@Result(property = "pkJpName",column = "pk_jp_name"),
			@Result(property = "pkEnName",column = "pk_en_name"),
			@Result(property = "pkSeq",column = "pk_seq"),
			@Result(property = "createTime",column = "create_time"),
			@Result(property = "createUserId",column = "create_user_id"),
			@Result(property = "updateTime",column = "update_time")
	})
	@Select("select * from cmd_sv_pk where id = #{id}")
	public CmdSvPk selectPkById(@Param("id") long id);
	/**
	 * 根据名称查询卡包
	 * @param pkName
	 * @param pkAlias
	 * @param pkJpName
	 * @param pkEnName
	 * @return
	 */
	@ResultMap("cmdSvPkMapper")
	@Select("select * from cmd_sv_pk where pk_name = #{pkName} or pk_alias = #{pkAlias} or pk_jp_name = #{pkJpName} or pk_en_name = #{pkEnName}")
	public CmdSvPk selectPkByName(String pkName,String pkAlias,String pkJpName,String pkEnName);
	
	@Insert("insert into cmd_sv_pk(pk_name,pk_alias,pk_jp_name,pk_en_name,pk_seq,create_time,create_user_id,update_time) "
			+ "values(#{pkName},#{pkAlias},#{pkJpName},#{pkEnName},#{pkSeq},#{createTime},#{createUserId},#{updateTime}) ")
	@Options(useGeneratedKeys = true,keyProperty = "id")
	public long insertPk(CmdSvPk pk);
	
	/**
	 * 获取最新的卡包
	 * @return
	 */
	@ResultMap("cmdSvPkMapper")
	@Select("select * from cmd_sv_pk order by pk_seq desc limit 1")
	public CmdSvPk getTopPk();
	
	@Results(id = "cmdSvCardMapper",value = {
			@Result(property = "id",column = "id"),
			@Result(property = "pkId",column = "pk_id"),
			@Result(property = "cardName",column = "card_name"),
			@Result(property = "cardNameJp",column = "card_name_jp"),
			@Result(property = "career",column = "career"),
			@Result(property = "cardRarity",column = "card_rarity"),
			@Result(property = "probability",column = "probability"),
			@Result(property = "createTime",column = "create_time"),
			@Result(property = "createUserId",column = "create_user_id"),
			@Result(property = "status",column = "status")
	})
	@Select("select * from cmd_sv_card where id = #{id}")
	public CmdSvCard selectCardById(@Param("id")long id);
	
	/**
	 * 查询一个卡包的所有卡
	 * @param pkId
	 * @return
	 */
	@ResultMap("cmdSvCardMapper")
	@Select("select * from cmd_sv_card where pk_id = #{pkId} and status = 1 ")
	public List<CmdSvCard> findCardByPk(@Param("pkId")long pkId);
	
	@ResultMap("cmdSvCardMapper")
	@Insert("insert into cmd_sv_card(pk_id,card_name,card_name_jp,career,card_rarity,probability,create_time,create_user_id,status) "
			+ "values(#{pkId},#{cardName},#{cardNameJp},#{career},#{cardRarity},#{probability},#{create_time},#{create_user_id},#{status})")
	public long insertCard(CmdSvCard card);
}