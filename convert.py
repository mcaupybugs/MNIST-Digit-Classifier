import numpy as np
from keras.preprocessing import image
import tensorflow as tf
from tensorflow import keras

classifier = keras.models.load_model('./Model/digit_recog.h5')

#model = tf.keras.load_model('model.h5')

# keras_file = "keras_model.h5"
# keras.model.save_model(model, keras_file)

converter = tf.lite.TFLiteConverter.from_keras_model(classifier)
tflite_model = converter.convert()
open("converted_model.tflite", "wb").write(tflite_model)
