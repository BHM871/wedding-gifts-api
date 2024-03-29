package com.example.wedding_gifts_api.core.domain.model;

import java.util.UUID;

import com.example.wedding_gifts_api.infra.dtos.image.SaveImageDTO;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_image")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String pathImage;

    @ManyToOne()
    @JoinColumn(name = "gift_id")
    private Gift gift;

    public Image(SaveImageDTO saveImage) throws Exception {
        if(saveImage.pathImage() == null) throw new Exception("Error in save image");

        this.pathImage = saveImage.pathImage();
    }
    
}
