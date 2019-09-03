package com.example.demo.controller;
import com.example.demo.entity.UploadFile;
import com.example.demo.entity.UploadFileRepository;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Date;
import java.util.Optional;

@Controller
public class FileController {
    @Autowired
    private UploadFileRepository uploadFileRepository;

    @PostMapping("/file/uploadImage")
    @ResponseBody
    public String uploadImage(@RequestParam(value="image") MultipartFile file){
        if(file.isEmpty()){
            return "1";
        }
        String fileName=file.getOriginalFilename();
        try {
            UploadFile uploadFile=new UploadFile();
            uploadFile.setName(fileName);
            uploadFile.setCreateTime(new Date());
            uploadFile.setContent(new Binary(file.getBytes()));
            uploadFile.setContentType(file.getContentType());
            uploadFile.setSize(file.getSize());

            UploadFile savedFile=uploadFileRepository.save(uploadFile);
            String url="/file/image/"+savedFile.getId();
        }catch (IOException e){
            e.printStackTrace();
            return "2";
        }
        return "0";
    }

    @GetMapping(value = "/file/image/{id}", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    @ResponseBody
    public byte[] image(@PathVariable String id){
        byte[] data = null;
        Optional<UploadFile> file=uploadFileRepository.findById(id);
        if(file.isPresent()){
             data = file.get().getContent().getData();
        }
        return data;
    }
}
