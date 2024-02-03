package config.data;

import java.util.List;
import java.util.Map;

@TwoMapper
public interface SampleTwoMapper {

  List<Map<String, Object>> getNames2();

}
