# ImageRetrieval
With the large-scale image database in the field of science and medicine, as well as in the field of advertising and marketing, it becomes very important to organize the image database and the effective retrieval method. This paper mainly introduces the B/S architecture, focuses on Content-Based Images Retrieval technology, introduces the basic features of image low-level acquisition and corresponding retrieval matching algorithm, including graphics color, local texture and shape characteristics, the overall work summarized as follows: The main work of this paper can be divided into three parts: the first part focuses on the extraction of RGB and HSV two color space one-and three-dimensional color histogram features, and the use of Pap coefficient method and Euclidean distance method to calculate the similarity of different images; in the second part, we use the improved "joint mode" to obtain the texture characteristics of each part of the image by using Locality Binary Pattern. Uniform Pattern, extracting image texture features, using Euclidean distance to calculate image similarity; The third part studies the Shape feature extraction method based on image Edge Direction Histogram, the feature vectors obtained by this method satisfy the size transformation of different graphs, the translation of images and the invariant characteristics of rotation. Based on the study of the three kinds of feature extraction algorithms, this system uses the Struts2 framework based on B/S architecture, implements the different algorithms using the Java programming language, and completes the content-based image retrieval system. The System tested Image Library contains 2400 commonly used test images, which can be retrieved in the form of local uploaded images. The search conditions for the various image features described above, this article elaborated on the different characteristics of flowers, beaches, buses, elephants and other categories of image retrieval effects, and analysis of different search methods and the advantages and disadvantages of the relevant improvement methods.
Web页面代码无法上传，需要的请联系我。656008660@qq.com
