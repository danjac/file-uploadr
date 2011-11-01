(ns file-uploadr.utils.image
  (:import java.awt.image.BufferedImage
           java.awt.Graphics2D
           java.awt.Color
           java.awt.RenderingHints
           java.io.File
           javax.imageio.ImageIO))


(defn image-obj [path]
  (javax.imageio.ImageIO/read (new File path)))


(defn image-size-from-obj [image]
  [(.getWidth image) 
   (.getHeight image)])


(defn image-size [path]
  (image-size-from-obj (image-obj path)))


(defn get-thumbnail-dimensions [image-width image-height thumb-width thumb-height]
  "Get the correct dimensions for the thumbnail"
   (let [image-ratio (/ image-width image-height)
         thumb-ratio (/ thumb-width thumb-height)
         is-smaller-ratio (< thumb-ratio image-ratio)
         adjusted-thumb-height (if is-smaller-ratio (/ thumb-width image-ratio) thumb-height)
         adjusted-thumb-width (if is-smaller-ratio thumb-width (* thumb-height image-ratio))]
     (cond (and (< image-width adjusted-thumb-width) 
                 (< image-height adjusted-thumb-height))
             [image-width image-height]
             (< image-width adjusted-thumb-width)
             [image-width adjusted-thumb-height]
             (< image-height adjusted-thumb-height)
             [adjusted-thumb-width image-height]
             :else [adjusted-thumb-width adjusted-thumb-height])))



(defn generate-thumbnail [image thumb-path thumb-width thumb-height]
  "Write the thumbnail to file"
  (let [buf (new java.awt.image.BufferedImage thumb-width 
                                              thumb-height 
                                              java.awt.image.BufferedImage/TYPE_INT_RGB)
        g2d (.createGraphics buf)]
    (.setBackground g2d java.awt.Color/WHITE)
    (.setPaint g2d java.awt.Color/WHITE)
    (.fillRect g2d 0 0 thumb-width thumb-height)
    (.setRenderingHint g2d java.awt.RenderingHints/KEY_INTERPOLATION
                           java.awt.RenderingHints/VALUE_INTERPOLATION_BILINEAR)
    (.drawImage g2d image 0 0 thumb-width thumb-height nil)
    (javax.imageio.ImageIO/write buf "JPG" (new File thumb-path))))

    

(defn create-thumbnail [image-path thumb-path thumb-width thumb-height]
  (let [image (image-obj image-path)
        [image-width image-height] (image-size-from-obj image)
        [thumb-width thumb-height] (get-thumbnail-dimensions image-width 
                                                             image-height 
                                                             thumb-width 
                                                             thumb-height)] 
    (generate-thumbnail image thumb-path thumb-width thumb-height)
    [thumb-width thumb-height]))
  
      

               
