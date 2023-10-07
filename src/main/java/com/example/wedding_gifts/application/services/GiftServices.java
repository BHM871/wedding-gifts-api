package com.example.wedding_gifts.application.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.wedding_gifts.core.domain.dtos.gift.CreateGiftDTO;
import com.example.wedding_gifts.core.domain.dtos.gift.DeleteGiftDTO;
import com.example.wedding_gifts.core.domain.dtos.gift.GiftResponseDTO;
import com.example.wedding_gifts.core.domain.dtos.gift.UpdateGiftDTO;
import com.example.wedding_gifts.core.domain.dtos.gift.searchers.SearcherByCategoriesAndPriceDTO;
import com.example.wedding_gifts.core.domain.dtos.gift.searchers.SearcherByCategoriesDTO;
import com.example.wedding_gifts.core.domain.dtos.gift.searchers.SearcherByPriceDTO;
import com.example.wedding_gifts.core.domain.dtos.gift.searchers.SearcherByTitleAndCategoriesDTO;
import com.example.wedding_gifts.core.domain.dtos.gift.searchers.SearcherByTitleAndPriceDTO;
import com.example.wedding_gifts.core.domain.dtos.gift.searchers.SearcherByTitleDTO;
import com.example.wedding_gifts.core.domain.dtos.gift.searchers.SearcherDTO;
import com.example.wedding_gifts.core.domain.dtos.image.DeleteImageDTO;
import com.example.wedding_gifts.core.domain.dtos.image.ImageDTO;
import com.example.wedding_gifts.core.domain.dtos.image.ImageResponseDTO;
import com.example.wedding_gifts.core.domain.dtos.image.RemoveImagesDTO;
import com.example.wedding_gifts.core.domain.dtos.image.AddImagesDTO;
import com.example.wedding_gifts.core.domain.model.Gift;
import com.example.wedding_gifts.core.domain.model.Image;
import com.example.wedding_gifts.core.usecases.gift.IGiftRepository;
import com.example.wedding_gifts.core.usecases.gift.IGiftUseCase;
import com.example.wedding_gifts.core.usecases.image.IImageUseCase;

@Service
public class GiftServices implements IGiftUseCase {

    @Autowired
    IGiftRepository repository;
    @Autowired
    IImageUseCase imageService;
    
    @Override
    public GiftResponseDTO createGift(CreateGiftDTO gift) throws Exception {
        Gift newGift = repository.createGift(gift);

        List<ImageResponseDTO> images = new ArrayList<ImageResponseDTO>();
        for(MultipartFile image : gift.images()) {

            if(
                image.getContentType() == null || !image.getContentType().startsWith("image")
            ) throw new Exception(image.getOriginalFilename()+" is not image");

            Image savedImage = imageService.saveImage(
                    new ImageDTO(image,
                    newGift.getId(), newGift.getAccount().getId())
                );

            images.add(
                new ImageResponseDTO(savedImage.getId(), savedImage.getPathImage())
            );
        }

        return new GiftResponseDTO(newGift, images);
    }

    @Override
    public void updateGift(UpdateGiftDTO gift) throws Exception {
        repository.updateGift(gift);        
    }

    @Override
    public void updateGift(AddImagesDTO images) throws Exception {
        if(images.imagesFile() != null) {
            for(MultipartFile image : images.imagesFile()) {
                imageService.saveImage(
                    new ImageDTO(image, images.giftId(), images.accountId())
                );
            }
        }
    }

    @Override
    public void updateGift(RemoveImagesDTO images) throws Exception {
        if(images.imagesId() != null) {
            for(UUID imageId : images.imagesId()) {
                imageService.deleteImage(
                    new DeleteImageDTO(imageId, images.giftId(), images.accountId())
                );
            }
        } 
    }

    @Override
    public void deleteGift(DeleteGiftDTO deleteGift) throws Exception {

        List<Image> imagesByGift = imageService.getAllByGift(deleteGift.giftId());
        for(Image image : imagesByGift) {
            imageService.deleteImage(
                new DeleteImageDTO(image.getId(), deleteGift.giftId(), deleteGift.accountId())
            );
        }

        repository.deleteGift(deleteGift);
    }

    @Override
    public List<GiftResponseDTO> getAllGifts(UUID accountId) throws Exception {
        List<Gift> gifts = repository.getAllGifts(accountId);

        return generatedGiftResponse(gifts);
    }

    @Override
    public List<GiftResponseDTO> getWithFilter(SearcherDTO searcher, UUID accountId) throws Exception {
        List<Gift> gifts = new ArrayList<Gift>();
        
        if(searcher.title() != null && searcher.categories() != null && (searcher.startPrice() != null || searcher.endPrice() != null)){ 
            gifts.addAll(repository.getAllFilters(searcherDTO(searcher), accountId));
        } else 
        if(searcher.title() != null){
            if(searcher.categories() != null) {
                gifts.addAll(repository.getByTitleAndCategoriesOrBought(searcherByTitleAndCategoriesDTO(searcher), accountId));
            } else 
            if(searcher.startPrice() != null || searcher.endPrice() != null) {
                gifts.addAll(repository.getByTitleAndPriceOrBought(searcherByTitleAndPriceDTO(searcher), accountId));
            } else {
                gifts.addAll(repository.getByTitleOrBoutght(searcherByTitleDTO(searcher), accountId));
            }

        } else 
        if(searcher.categories() != null) {
            if(searcher.startPrice() != null || searcher.endPrice() != null) {
                gifts.addAll(repository.getByCategoriesAndPriceOrBought(searcherByCategoriesAndPriceDTO(searcher), accountId));
            } else {
                gifts.addAll(repository.getByCategoriesOrBought(searcherByCategoriesDTO(searcher), accountId));
            }

        } else 
        if(searcher.startPrice() != null || searcher.endPrice() != null) {
            gifts.addAll(repository.getByPriceOrBought(searcherByPriceDTO(searcher), accountId));
        
        } else {
            throw new Exception("Filters are null");
        }

        return generatedGiftResponse(gifts);
    }

    private SearcherDTO searcherDTO(SearcherDTO searcher) {
        return new SearcherDTO(
            searcher.isBought() != null ? searcher.isBought() : null, 
            searcher.title(), 
            searcher.startPrice() != null ? searcher.startPrice() : BigDecimal.ZERO, 
            searcher.endPrice() != null ? searcher.endPrice() : BigDecimal.valueOf(Double.MAX_VALUE),
            searcher.categories() 
        );
    }

    private SearcherByTitleAndCategoriesDTO searcherByTitleAndCategoriesDTO(SearcherDTO searcher) {
        return new SearcherByTitleAndCategoriesDTO(
            searcher.title(), 
            searcher.categories(), 
            searcher.isBought() != null ? searcher.isBought() : null
        );
    }

    private SearcherByTitleAndPriceDTO searcherByTitleAndPriceDTO(SearcherDTO searcher) {
        return new SearcherByTitleAndPriceDTO(
            searcher.title(), 
            searcher.startPrice() != null ? searcher.startPrice() : BigDecimal.ZERO, 
            searcher.endPrice() != null ? searcher.endPrice() : BigDecimal.valueOf(Double.MAX_VALUE),
            searcher.isBought() != null ? searcher.isBought() : null 
        );
    }

    private SearcherByTitleDTO searcherByTitleDTO(SearcherDTO searcher) {
        return new SearcherByTitleDTO(
            searcher.title(), 
            searcher.isBought() != null ? searcher.isBought() : null
        );
    }

    private SearcherByCategoriesAndPriceDTO searcherByCategoriesAndPriceDTO(SearcherDTO searcher) {
        return new SearcherByCategoriesAndPriceDTO(
            searcher.categories(), 
            searcher.startPrice() != null ? searcher.startPrice() : BigDecimal.ZERO, 
            searcher.endPrice() != null ? searcher.endPrice() : BigDecimal.valueOf(Double.MAX_VALUE),
            searcher.isBought() != null ? searcher.isBought() : null 
        );
    }

    private SearcherByCategoriesDTO searcherByCategoriesDTO(SearcherDTO searcher) {
        return new SearcherByCategoriesDTO(
            searcher.categories(), 
            searcher.isBought() != null ? searcher.isBought() : null
        );
    }

    private SearcherByPriceDTO searcherByPriceDTO(SearcherDTO searcher) {
        return new SearcherByPriceDTO(
            searcher.startPrice() != null ? searcher.startPrice() : BigDecimal.ZERO, 
            searcher.endPrice() != null ? searcher.endPrice() : BigDecimal.valueOf(Double.MAX_VALUE),
            searcher.isBought() != null ? searcher.isBought() : null 
        );
    }

    private List<GiftResponseDTO> generatedGiftResponse(List<Gift> gifts) throws Exception {
        List<List<Image>> imagesByGifts = new ArrayList<List<Image>>();
        for(Gift gift : gifts) {
            List<Image> images = imageService.getAllByGift(gift.getId());
            imagesByGifts.add(images);
        }

        List<List<ImageResponseDTO>> imagesResponse = new ArrayList<List<ImageResponseDTO>>();
        for(List<Image> images : imagesByGifts) {
            List<ImageResponseDTO> temp = images
                                            .stream()
                                            .map(image -> new ImageResponseDTO(image.getId(), image.getPathImage()))
                                            .toList();
            imagesResponse.add(temp);
        }

        List<GiftResponseDTO> giftResponseList = new ArrayList<GiftResponseDTO>();
        for(int i = 0; i < gifts.size(); i++) {
            GiftResponseDTO giftResponse = new GiftResponseDTO(gifts.get(i), imagesResponse.get(i));
            giftResponseList.add(giftResponse);
        }

        return giftResponseList;
    }
    
}
