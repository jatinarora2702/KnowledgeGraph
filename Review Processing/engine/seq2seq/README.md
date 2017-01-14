# Sequence to Sequence Modelling

Here, we use the language translation model from English to French in Tensorflow. We define the following mapping:

English Sentence <-> Comparison-based Review Sentence

French Sentence <-> Entity Tagged Sentence

# Tags Used

We define 5 classes in which a word token can lie in:

1. Product1
2. Product2
3. Aspect
4. Opinion
5. None

# Example

**Source:**
Nikon CoolPix has better lens resolution than Cannon Powershot

**target:**
PRODUCT1 PRODUCT1 NONE OPINION ASPECT ASPECT NONE PRODUCT2 PRODUCT2
