/* Copyright 2021 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================
*/

package com.example.boogi_trainer.ml

import android.content.Context
import org.tensorflow.lite.Interpreter
import com.example.boogi_trainer.data.Person
import org.tensorflow.lite.support.common.FileUtil

class PoseClassifier(
    private val interpreter: Interpreter,
    private val labels: List<String>

) {
    private val input = interpreter.getInputTensor(0).shape()
    private val output = interpreter.getOutputTensor(0).shape()

    companion object {
       // private const val MODEL_FILENAME = "pushup_classifier.tflite"
       // private const val LABELS_FILENAME = "pushup_labels.txt"
        private const val CPU_NUM_THREADS = 4

        fun create(context: Context, MODEL_FILENAME: String, LABELS_FILENAME: String): PoseClassifier {
            val options = Interpreter.Options().apply {
                setNumThreads(CPU_NUM_THREADS)
            }
            return PoseClassifier(
                Interpreter(
                    FileUtil.loadMappedFile(
                        context, MODEL_FILENAME
                    ), options
                ),
                FileUtil.loadLabels(context, LABELS_FILENAME)
            )
        }
    }

    fun classify(person: Person?): List<Pair<String, Float>> {
        // Preprocess the pose estimation result to a flat array
        val inputVector = FloatArray(input[1])
        val inputVector_x = FloatArray(17)
        val inputVector_y = FloatArray(17)
        person?.keyPoints?.forEachIndexed { index, keyPoint ->
            inputVector_y[index] = keyPoint.coordinate.y
            //Log.d("y이게 안돼?", (keyPoint.coordinate.y).toString())
            inputVector_x[index] = keyPoint.coordinate.x
            //Log.d("x", (keyPoint.coordinate.x).toString())
            //inputVector[index * 3 + 2] = keyPoint.score
        }

        val min_x = inputVector_x.minOrNull()
        val max_x = inputVector_x.maxOrNull()
        val min_y = inputVector_y.minOrNull()
        val max_y = inputVector_y.maxOrNull()
        val avg_x = inputVector_x.average()
        val avg_y = inputVector_y.average()

        for(i in 0 until 17){
            if (max_x != null && avg_x != null) {
                inputVector_x[i] = ((inputVector_x[i] - avg_x) / (max_x - avg_x)).toFloat()
            }
            if (max_y != null && avg_y != null) {
                inputVector_y[i] = ((inputVector_y[i] - avg_y) / (max_y - avg_y)).toFloat()
            }
        }

        // Log.d("max_y", inputVector_y[16].toString())

        // Postprocess the model output to human readable class names
        val outputTensor = FloatArray(output[1])
        interpreter.run(arrayOf(inputVector_y+inputVector_x), arrayOf(outputTensor))
        val output = mutableListOf<Pair<String, Float>>()
        outputTensor.forEachIndexed { index, score ->
            output.add(Pair(labels[index], score))
        }
        return output
    }

    fun close() {
        interpreter.close()
    }
}
