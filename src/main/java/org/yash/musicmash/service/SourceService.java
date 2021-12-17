package org.yash.musicmash.service;

import java.util.Map;
import org.yash.musicmash.data.MusicMash;
import org.yash.musicmash.exception.MusicMashServiceException;

public interface SourceService {

  /**
   * The GetDataFromSource, accepts the params which are required to get data from backend. The
   * RequestParameter Map, provides the flexibility to the implementing classes to send differnt
   * parameters, as requested by different backends.
   *
   * @param requestParams Map of key/value pairs
   * @return <MusicMash>
   * @throws MusicMashServiceException
   */
  MusicMash getDataFromSource(Map<String, Object> requestParams) throws MusicMashServiceException;
}
