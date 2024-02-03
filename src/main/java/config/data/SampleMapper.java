package config.data;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Sample Mapper
 *
 * @author JI YOONSEONG
 **/
@Mapper
public interface SampleMapper {

  List<SampleVo> getNames();

}
