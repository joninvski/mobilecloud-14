/*
 *
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.magnum.dataup;

import org.springframework.stereotype.Controller;
import java.util.List;
import java.util.ArrayList;
import org.magnum.dataup.model.Video;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.Collection;
import java.util.UUID;
import org.magnum.dataup.model.VideoStatus;
import org.springframework.web.bind.annotation.PathVariable;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.io.IOException;

@Controller
public class VideoController {

    private List<Video> videos = new ArrayList<Video>();

    /*
    * GET /video
    *   - Returns the list of videos that have been added to the
    *     server as JSON. The list of videos does not have to be
    *     persisted across restarts of the server. The list of
    *     Video objects should be able to be unmarshalled by the
    *     client into a Collection<Video>.
    **/
    @RequestMapping( value="/video", method=RequestMethod.GET )
    public @ResponseBody Collection<Video> getVideo( ) {
        return videos;
    }

    /*
    * POST /video
    *   - The video data is provided as an application/json request
    *     body. The JSON should generate a valid instance of the
    *     Video class when deserialized by Spring's default
    *     Jackson library.
    *   - Returns the JSON representation of the Video object that
    *     was stored along with any updates to that object.
    *     --The server should generate a unique identifier for the Video
    *     object and assign it to the Video by calling its setId(...)
    *     method. The returned Video JSON should include this server-generated
    *     identifier so that the client can refer to it when uploading the
    *     binary mpeg video content for the Video.
    *    -- The server should also generate a "data url" for the
    *     Video. The "data url" is the url of the binary data for a
    *     Video (e.g., the raw mpeg data). The URL should be the *full* URL
    *     for the video and not just the path. You can use a method like the
    *     following to figure out the name of your server:
    *
    *     	private String getUrlBaseForLocalServer() {
    *		   HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    *		   String base = "http://"+request.getServerName()+((request.getServerPort() != 80) ? ":"+request.getServerPort() : "");
    *		   return base;
    *		}
    **/
    @RequestMapping( value="/video", method=RequestMethod.POST )
    public @ResponseBody Video addVideo( @RequestBody Video v ) {
        long id = UUID.randomUUID().getLeastSignificantBits();

        v.setId( id );
        String baseUrl = getUrlBaseForLocalServer() + "/" + Integer.toString( v.hashCode() );
        v.setDataUrl( baseUrl );

        videos.add( v );
        return v;
    }

    /*
     * POST /video/{id}/data
     *   - The binary mpeg data for the video should be provided in a multipart
     *     request as a part with the key "data". The id in the path should be
     *     replaced with the unique identifier generated by the server for the
     *     Video. A client MUST *create* a Video first by sending a POST to /video
     *     and getting the identifier for the newly created Video object before
     *     sending a POST to /video/{id}/data.
     **/
    @RequestMapping( value="/video/{id}/data", method=RequestMethod.POST )
    public @ResponseBody VideoStatus addVideoData(
        @PathVariable( "id" )  long id,
        MultipartFile videoData )
    throws ResourceNotFoundException {

        for ( Video v : videos ) {
            if( v.getId() == id ) {
                try { 
                    InputStream in = videoData.getInputStream();
                }
                catch ( IOException e ) {
                
                }
            }
        }

        throw new ResourceNotFoundException("Id: " + id + " does not exist");


        // v.setId( id );
        // videos.add( v );
        // return new VideoStatus( VideoStatus.VideoState.PROCESSING );
    }

    /*
     * GET /video/{id}/data
     *   - Returns the binary mpeg data (if any) for the video with the given
     *     identifier. If no mpeg data has been uploaded for the specified video,
     *     then the server should return a 404 status code.
     *
     **/
    @RequestMapping( value="/video/{id}/data", method=RequestMethod.GET )
    public @ResponseBody VideoStatus getVideoData( @PathVariable( "id" )  long id ) {

        for ( Video v : videos ) {
            if( v.getId() == id ) {
                return null;
            }
        }

        throw new ResourceNotFoundException("Id: " + id + " does not exist");

        // throw new Exception( "Video with id" + id + " does not exist" );
    }

    private String getUrlBaseForLocalServer() {
        HttpServletRequest request = ( ( ServletRequestAttributes ) RequestContextHolder.getRequestAttributes() ).getRequest();
        String base = "http://"+request.getServerName()+( ( request.getServerPort() != 80 ) ? ":"+request.getServerPort() : "" );
        return base;
    }

    @ResponseStatus( value = HttpStatus.NOT_FOUND )
    public class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException( String s ) {
            super( s );
        }
    }

}
