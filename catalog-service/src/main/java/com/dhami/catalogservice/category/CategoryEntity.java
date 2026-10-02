package com.dhami.catalogservice.category;

import com.dhami.catalogservice.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "categories")
public class CategoryEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;
}

/*

Slug -> A slug is a short, readable, URL-friendly version of a name or title.
name -> Mobile Phones, slug -> mobile-phones
url -> https://example.com/categories/mobile-phones

Why do we use a slug?
Makes URLs easy to read->
bad -> /categories?id=5
good -> /categories/mobile-phones

Better for SEO (Search Engines)
Search engines understand meaningful words in URLs.

Easy to identify records
Users can tell what the page is about just by looking at the URL.
 */
