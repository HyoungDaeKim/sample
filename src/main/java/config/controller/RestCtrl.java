package config.controller;

import config.data.SampleMapper;
import config.data.SampleTwoMapper;
import config.data.SampleVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api")
public class RestCtrl {

  final SampleMapper mapper;
  final SampleTwoMapper twoMapper;

  public RestCtrl(SampleMapper mapper, SampleTwoMapper twoMapper) {
    this.mapper = mapper;
    this.twoMapper = twoMapper;
  }

  @GetMapping(value = "/sample")
  public List<SampleVo> get() {
    return mapper.getNames();
  }

  @GetMapping(value = "/sample2")
  public List<Map<String, Object>> get2() {
    return twoMapper.getNames2();
  }

}
