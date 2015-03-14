# CORPUS
CORPUS is a *multisensor data fusion* framework for sensors tracking the human body or parts of it. In particular the framework tries to satisfy the following requirements.

### Sensor Abstraction
The framework is able to automatically fuse and filter the data delivered by the sensors. The result is a uniform body model composed of joints. It is possible to access the position, orientation and tracking state and more of a joint. Thus the developer doesn't need to deal with the specific details of the sensors or their API anymore. The only requirement is a implemented wrapper for the sensor, but this only needs to be done ones.

### Platformindependence
The framework is designed as a client-server structure. The client application can connect to the server though a RESTful webservice or a WebSocket connection. The body model is then sent to the client in form of a JSON string.

### Extensibility and Adaptability
The framework is highly extensible and adaptable. The fusing process is executed by a class extending the `Fuser` class. The framework provides example implementation of this class using general fusing strategies. However, it is possible to write custom implementations to adapt the fusing process. The same is true for the `Filter` class responsible for the filter process. Wrapper are also easy implementable by extending the `Sensor` class.
Furthermore the framework is configured through a configuration file. This enables the user to change the update rate, the base URI, the granularity of the body model and more.

### Support for Multiple Sensors
The update chain is designed to support multiple sensors. Only a wrapper for a sensor needs to be added to the framework though the configuration file.

## Installation
The framework is written in java and uses the [Grizzly Project] to implement the server. The REST Webservice is realized with the help of [Jersey]. Currently wrapper are available for the *Microsoft Kinect v1*, the *Leap Motion*, the *Oculus Rift Dev Kit 1* and *Dev Kit 2* and the *Myo*. To communicate with the sensors the libraries [Kinect-for-Java], [jovr] and [myo-java] are used.

The provided project is a eclipse project. To manage the dependencies, [Maven] is used. Therefore no additional libraries should be needed. The framework was tested with Windows 7 64 bit. This repository mainly includes native libraries for 64 bit Windows systems. Other libraries may be needed for different systems.

The build process is also managed by maven and can be executed by the following command:

```sh
mvn  clean  install
```
The folder `target` then contains a executable java jar file.

## Examples
* TODO: corpus client
* TODO: living room example
* TODO: piano simulator example

## Body Representation Format
In the context of the framework the human is represented in form of joints. The joints are defined in a hierarchical way. That means each joint has a parent. The connection between a joint and its parent can be interpreted as a bone, but is not explicitly stated in the body representation. The position and orientation of a joint is relative to the parent. The orientation describes how the coordinate system of the parent has to be rotated so that the bone created by the joint and his parent lies on the y-axis. If there is no parent, the joint is a root joint and the global coordinate system is considered.  The position of a joint uses the coordinate system of the parent. The orientations are represented as quaternions.

The body model is divided into 3 parts - *torso*, *hands* and *feet*. For each of the three parts the user can set the needed granularity. Namely these are *NONE*, *SPARSE* and *COMPLEX*. The following table describes the granularity levels in detail.

<table>
  <tr>
    <th>Body Part</th>
    <th>Granularity</th>
    <th>Contained Joints</th>
  </tr>
  <tr>
    <td rowspan="3">Torso</td>
    <td>NONE</td>
    <td>none</td>
  </tr>
  <tr>
    <td>SPARSE</td>
    <td>middle, left and right hip, knees, middle, left and right shoulder, elbow, head</td>
  </tr>
  <tr>
    <td>COMPLEX</td>
    <td>SPARSE + middle spine, neck</td>
  </tr>
  <tr>
    <td rowspan="3">Hands</td>
    <td>NONE</td>
    <td>none</td>
  </tr>
  <tr>
    <td>SPARSE</td>
    <td>wrists, center of the hands</td>
  </tr>
  <tr>
    <td>COMPLEX</td>
    <td>SPARSE + every moveable finger joint</td>
  </tr>
  <tr>
    <td rowspan="3">Feet</td>
    <td>NONE</td>
    <td>none</td>
  </tr>
  <tr>
    <td>SPARSE</td>
    <td>ankles, center of the feet</td>
  </tr>
  <tr>
    <td>COMPLEX</td>
    <td>SPARSE + every moveable toe joint </td>
  </tr>
</table>

Joints which are not detected by a sensor are set to their default position after a specified period of time. The following picture shows a body model with full complexity. All joints are in their default position. The center hip serves as the root joint due to its central position within the human body.

![Full body model in default position](https://github.com/scopus777/corpus/blob/master/img/full_model.png "Full body model in default position")

If a client application requests the current model, JSON data is generated. The following lines show a part of an exemplary JSON representation of the body model. 

```json
  [ { "jointType": "SPINE_BASE",
      "relativePosition": {
        "x": 0, "y": 0, "z": 300},
      "relativeOrientation": {
        "w": 0, "x": 0, "y": 1, "z": 0},
      "absolutePosition": {
        "x": 0, "y": 0, "z": 300},
      "absoluteOrientation": {
        "w": 0, "x": 0, "y": 1, "z": 0},
      "positionConfidence": 0,
      "orientationConfidence": 0,
      "positionTracked": false,
      "orientationTracked": false,
      "positionTimestamp": 1420629906784,
      "orientationTimestamp": 1420629906784,
      "children": [
        { "jointType": "HIP_RIGHT",
            ...},
          ...] } ]
```

The model is returned as a list of root joints (joints without a father). In this sample case the only root joint in the current model is the joint with the type SPINE\_BASE. The relative and absolute values are the same because there is no father for this joint. The confidence values are set by the sensor wrapper and may be manipulated by the used `Fuser`. In this case the values are set to zero because the joint is not tracked as stated in the following line. The timestamp states the point in time when the joint was created (as UTC milliseconds from the epoch). The children are serialized in the same way as described for the root joint. It is not mandatory that all of the attributes shown are transferred. It is also possible to define in the REST request which attributes are required. The following REST requests are provided by the framework:

| Method                | Arguments                   | Description                                                                                                                                          |
|-----------------------|-----------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------|
| fullHierarchicalModel | none                        | Sends the model in hierarchical form.                                                                                                                |
| fullListModel         | none                        | Sends the model in form of a list.                                                                                                                   |
| customModel           | type, field                 | Sends all joints contained by the body model but allows to choose the typ of the model (list orhierarchical) and the fields that will be serialized. |
| singleJoints          | jointType, field            | Sends the joints whose types are defines by the jointType attribute. The serialized fields are again defined by the field argument.             |
| sensors               | none                        | Sends all active sensors.                                                                                                                            |
| callSensorMethod      | sensorId, methodName, param | Enables the user to call a sensor specific method.                                                                                                   |

All methods besides the last one are realized as REST GET opertaions. The following request for example asks for the joint with the type HEAD and the fields jointType, absolutePosition and absoluteOrientation:
```http
http://localhost:8080/corpus/singleJoint?jointType=HEAD&field=jointType&field=absolutePosition&field=absoluteOrientation
```

The last method enables the user to call a sensor specific method. This method has of course to be implemented in the corresponding wrapper. The method should be used only if necessary, because contradicts the sensor abstraction targeted by the framework.

## Software Structure
![Framework Processes](https://github.com/scopus777/corpus/blob/master/img/framework_processes.png "Framework Processesn")
### Processes
The image shows how a client request is handled, including the starting process of the server and the update process of the model. The `Controller` starts the server and handles client requests. The update process is independent of any request and is run by the `SceneController` multiple times per second. The specific update rate can be adopted to the requirements of the client application. The update process includes the call of the `Fuser` which collects the data from the sensors by itself and fuses the received data in a specific manner. How the data is fused in detail depends on the chosen implementation of the `Fuser`. The fused data is then passed to the Filter. How the data is filtered again depends on the implementation of the `Filter`. The `SceneController` then can generate a JSON string containing the current model which is returned by the `Controller` to the client. The important components of the framework are explained in detail in the following section.

### Classes
#### Controller
The `Controller` is the main component of the framework. It starts the server which includes the parsing of the configuration file. The configuration file can for example be used to customize the body model or to change the update rate of the model. The `Controller` also handles the client requests and initializes the `SceneController`

#### SceneController and Scene
The `SceneController` runs in its own thread and contains and updates the current `Scene`. A `Scene` contains the positions and orientations of the sensors and the joints at a specific point in time. In the configuration file the user has to define an initial `Scene`. This `Scene` enables the user to define the used sensors and the body model. The sensors are also placed in the `Scene` because their positions and orientations can change during the runtime of the framework - for example if a sensor is placed relative to a joint. The `SceneController` also generates the JSON representation of the current `Scene` and contains a history of elapsed `Scene`s. This history can be used during the filter process or by client applications. Currently only a single Scene is supported.

#### SceneNode
The `SceneNode` is an abstract class and represents objects that can be placed in the `Scene`. A scene node can have a parent. The fields `relativePosition` and `relativeOrientation` contain relative values to this parent like explained the chapter for the body model. The fields `absolutePosition` and `absoluteOrientation` accordingly contain the absolute values. The setter and getter for the relative and absolute values are design to for consistence. If a relative value is updated the absolute value will be updated to if necessary and vice versa.

#### Joint
A `Joint` extends the `SceneNode` class and represents a brick of the body model. The type of the joint is indicated by the `JointType`. Additionally to the fields of the `SceneNode` class a `Joint` contains information about the tracking state and the confidence of the tracked values. It also contains a default position and orientation.

#### Sensor
The framework provides an abstract `Sensor` class which can be implemented for any sensor providing positional or orientational data of human body regions. A `Sensor` extends the `SceneNode` class. A `Sensor` serves two purposes in the context of the framework. Firstly, it is a wrapper communicating with the actual sensor and processing the delivered data. Secondly, a `Sensor` (the wrapper) is also placed in the `Scene`. Therefore, it also represents the actual sensor in the context of the framework.

### Fuser
One `Fuser` per Scene is responsible for merging the data delivered by multiple sensors. It is possible to create a custom implementation. In the following, the fusing process is described on the basis of the `ConfidenceDominanceFuser` provided by the framework. The `Fuser` collects the newest data of the sensors and expects that the confidence for the position and orientation is set. On the basis of these values, the `Fuser` now determines which data will be taken. If no sensor provides data for a specific joint, the data of the last `Scene` will be preserved or the position of the joint is reset to its default position if there was no new data for a specific period of time. If only one sensor provides new data for a joint, the data is taken and if multiple sensors provide data for a joint, the data with the highest confidence value is taken. The confidence value of the final joint is the one of the chosen joint. This procedure is repeated for every joint in the model. Thus, data for joints not contained in the model is ignored.

### Filter 
The `Scene` with the fused positions and orientations is forwarded to the `Filter`. The `Filter` can be implemented by the user and there is no restriction on how the data is filtered. Inverse kinematics could be used to ensure that the position of the joints correlate with the potential moving space of the human body. Likewise, a `Filter` could smooth the data to reduce the noise. Exemplary, a double exponential smoothing filter is used in this framework.


## Integration of New Sensors
To support upcoming sensors, it is important to make the integration of new sensors as easy as possible. The framework allows this by simply extending the abstract `Sensor` class. The `Sensor` class contains the field `currentData` representing a `Map` which can be set with the help of the method `setCurrentData`. The keys of the `Map` are `JointType`s and the values are `Joint`s. This representation simplifies the fusion and filter process. Hence, the job of the wrapper is to map the collected data to the `Joint`s supported by the framework. It is possible to set the relative values, but then the parents are expected too. The confidence values should be set as well if a Fuser expecting these values is used.

There are three ways to get the data from the actual sensor. Firstly, it is possible to directly request the new data from the sensor when needed. Once in the update cycle the `getCurrentData` method is called for every wrapper. This method automatically calls the function `updateCurrentData`. This method has to be implemented by the user and can be used to get the current data from the sensor. Secondly, a lot of devices work with an event based system, where new sensor data triggers an event to notify interested listeners. If a custom sensor wrapper uses this kind of update process, the listener should set the current data map in response to an event. In this case the `updateCurrentData` method can be left empty. The last option is to use the `run` method. Every wrapper runs in its own thread. In the `run` method it is possible to implement a loop continuously polling the data from the sensor.
A event based system and continuously polling the data increases the scalability of the framework because the polling process is moved away from the update process. On the other hand, polling the data only when needed probably saves unnecessary calls if the update rate of the sensor is higher than the one of the framework.

## Configuration File

The configuration file (config.xml) is an XML file enabling the user to customize the framework. The committed configuration file commented and is therefore not further explained at this point.

## Joint Types
The frameworks lists every possible joint type in the enum `JointType`. In the following the joint types will be explained.

## Hands
![Joints of the left hand](https://github.com/scopus777/corpus/blob/master/img/hand_joints.png "Joints of the left hand")

The image (Source: Modified version of [LadyofHats]) visualizes the joints for the left hand used in the framework. The following table maps the joint to the corresponding `JointType` value. For the right hand `LEFT` has to be simply replaced by `RIGHT`.

<table>
  <tr>
    <th>JointType</th>
    <th>Number</th>
    <th>Joint</th>
  </tr>
  <tr>
    <td>WRIST_LEFT</td>
    <td>1</td>
    <td>left wrist</td>
  </tr>
  <tr>
    <td>HAND_CENTER_LEFT</td>
    <td>2</td>
    <td>center of the left palm</td>
  </tr>
  <tr>
    <td colspan="3">left small finger</td>
  </tr>
  <tr>
    <td>CMC_SMALL_FINGER_LEFT</td>
    <td>3</td>
    <td>carpometacarpal (CMC) joint</td>
  </tr>
  <tr>
    <td>MCP_SMALL_FINGER_LEFT</td>
    <td>4</td>
    <td>metacarpophalangeal (MCP) joint</td>
  </tr>
  <tr>
    <td>PIP_SMALL_FINGER_LEFT</td>
    <td>5</td>
    <td>proximal interphalangeal (PIP) joint</td>
  </tr>
  <tr>
    <td>DIP_SMALL_FINGER_LEFT</td>
    <td>6</td>
    <td>distal interphalangeal (DIP) joint</td>
  </tr>
  <tr>
    <td>BTIP_SMALL_FINGER_LEFT</td>
    <td>7</td>
    <td>bone tip (BTIP)</td>
  </tr>
  <tr>
    <td colspan="3">left ring finger</td>
  </tr>
  <tr>
    <td>CMC_RING_FINGER_LEFT</td>
    <td>8</td>
    <td>carpometacarpal (CMC) joint</td>
  </tr>
  <tr>
    <td>MCP_RING_FINGER_LEFT</td>
    <td>9</td>
    <td>metacarpophalangeal (MCP) joint</td>
  </tr>
  <tr>
    <td>PIP_RING_FINGER_LEFT</td>
    <td>10</td>
    <td>proximal interphalangeal (PIP) joint</td>
  </tr>
  <tr>
    <td>DIP_RING_FINGER_LEFT</td>
    <td>11</td>
    <td>distal interphalangeal (DIP) joint</td>
  </tr>
  <tr>
    <td>BTIP_RING_FINGER_LEFT</td>
    <td>12</td>
    <td>bone tip (BTIP)</td>
  </tr>
  <tr>
    <td colspan="3">left middle finger</td>
  </tr>
  <tr>
    <td>CMC_MIDDLE_FINGER_LEFT</td>
    <td>13</td>
    <td>carpometacarpal (CMC) joint</td>
  </tr>
  <tr>
    <td>MCP_MIDDLE_FINGER_LEFT</td>
    <td>14</td>
    <td>metacarpophalangeal (MCP) joint</td>
  </tr>
  <tr>
    <td>PIP_MIDDLE_FINGER_LEFT</td>
    <td>15</td>
    <td>proximal interphalangeal (PIP) joint</td>
  </tr>
  <tr>
    <td>DIP_MIDDLE_FINGER_LEFT</td>
    <td>16</td>
    <td>distal interphalangeal (DIP) joint</td>
  </tr>
  <tr>
    <td>BTIP_MIDDLE_FINGER_LEFT</td>
    <td>17</td>
    <td>bone tip (BTIP)</td>
  </tr>
  <tr>
    <td>left index finger</td>
    <td></td>
    <td></td>
  </tr>
  <tr>
    <td>CMC_INDEX_FINGER_LEFT</td>
    <td>18</td>
    <td>carpometacarpal (CMC) joint</td>
  </tr>
  <tr>
    <td>MCP_INDEX_FINGER_LEFT</td>
    <td>19</td>
    <td>metacarpophalangeal (MCP) joint</td>
  </tr>
  <tr>
    <td>PIP_INDEX_FINGER_LEFT</td>
    <td>20</td>
    <td>proximal interphalangeal (PIP) joint</td>
  </tr>
  <tr>
    <td>DIP_INDEX_FINGER_LEFT</td>
    <td>21</td>
    <td>distal interphalangeal (DIP) joint</td>
  </tr>
  <tr>
    <td>BTIP_INDEX_FINGER_LEFT</td>
    <td>22</td>
    <td>bone tip (BTIP)</td>
  </tr>
  <tr>
    <td>left thumb</td>
    <td></td>
    <td></td>
  </tr>
  <tr>
    <td>CMC_THUMB_LEFT</td>
    <td>23</td>
    <td>carpometacarpal (CMC) joint</td>
  </tr>
  <tr>
    <td>MCP_THUMB_LEFT</td>
    <td>24</td>
    <td>metacarpophalangeal (MCP) joint</td>
  </tr>
  <tr>
    <td>IP_THUMB_LEFT</td>
    <td>25</td>
    <td>interphalangeal (IP) joint</td>
  </tr>
  <tr>
    <td>BTIP_THUMB_LEFT</td>
    <td>26</td>
    <td>bone tip (BTIP)</td>
  </tr>
</table>

## Feet
![Joints of the left foot](https://github.com/scopus777/corpus/blob/master/img/feet_joints.png "Joints of the left foot")

The image (Source: Modified version of [Tasha]) visualizes the joints of the left foot used in the framework. The following table maps the joint to the corresponding `JointType` value. For the right foot `LEFT` has to be simply replaced by `RIGHT`.

<table>
  <tr>
    <th>JointType</th>
    <th>Number</th>
    <th>Joint</th>
  </tr>
  <tr>
    <td>ANKLE_LEFT</td>
    <td>1</td>
    <td>left ankle</td>
  </tr>
  <tr>
    <td>FOOT_CENTER_LEFT</td>
    <td>2</td>
    <td>center of the left foot</td>
  </tr>
  <tr>
    <td>HEEL_BONE_LEFT</td>
    <td>3</td>
    <td>heel bone of the left foot</td>
  </tr>
  <tr>
    <td colspan="3">left small toe</td>
  </tr>
  <tr>
    <td>TMT_SMALL_TOE_LEFT</td>
    <td>4</td>
    <td>tarsometatarsal (TMT) joint</td>
  </tr>
  <tr>
    <td>MCP_SMALL_TOE_LEFT</td>
    <td>5</td>
    <td>metacarpophalangeal (MCP) joint</td>
  </tr>
  <tr>
    <td>PIP_SMALL_TOE_LEFT</td>
    <td>6</td>
    <td>proximal interphalangeal (PIP) joint</td>
  </tr>
  <tr>
    <td>DIP_SMALL_TOE_LEFT</td>
    <td>7</td>
    <td>distal interphalangeal (DIP) joint</td>
  </tr>
  <tr>
    <td>BTIP_SMALL_TOE_LEFT</td>
    <td>8</td>
    <td>bone tip (BTIP)</td>
  </tr>
  <tr>
    <td colspan="3">left ring toe</td>
  </tr>
  <tr>
    <td>TMT_RING_TOE_LEFT</td>
    <td>9</td>
    <td>tarsometatarsal (TMT) joint</td>
  </tr>
  <tr>
    <td>MCP_RING_TOE_LEFT</td>
    <td>10</td>
    <td>metacarpophalangeal (MCP) joint</td>
  </tr>
  <tr>
    <td>PIP_RING_TOE_LEFT</td>
    <td>11</td>
    <td>proximal interphalangeal (PIP) joint</td>
  </tr>
  <tr>
    <td>DIP_RING_TOE_LEFT</td>
    <td>12</td>
    <td>distal interphalangeal (DIP) joint</td>
  </tr>
  <tr>
    <td>BTIP_RING_TOE_LEFT</td>
    <td>13</td>
    <td>bone tip (BTIP)</td>
  </tr>
  <tr>
    <td colspan="3">left middle toe</td>
  </tr>
  <tr>
    <td>TMT_MIDDLE_TOE_LEFT</td>
    <td>14</td>
    <td>tarsometatarsal (TMT) joint</td>
  </tr>
  <tr>
    <td>MCP_MIDDLE_TOE_LEFT</td>
    <td>15</td>
    <td>metacarpophalangeal (MCP) joint</td>
  </tr>
  <tr>
    <td>PIP_MIDDLE_TOE_LEFT</td>
    <td>16</td>
    <td>proximal interphalangeal (PIP) joint</td>
  </tr>
  <tr>
    <td>DIP_MIDDLE_TOE_LEFT</td>
    <td>17</td>
    <td>distal interphalangeal (DIP) joint</td>
  </tr>
  <tr>
    <td>BTIP_MIDDLE_TOE_LEFT</td>
    <td>18</td>
    <td>bone tip (BTIP)</td>
  </tr>
  <tr>
    <td colspan="3">left long toe</td>
  </tr>
  <tr>
    <td>TMT_LONG_TOE_LEFT</td>
    <td>19</td>
    <td>tarsometatarsal (TMT) joint</td>
  </tr>
  <tr>
    <td>MCP_LONG_TOE_LEFT</td>
    <td>20</td>
    <td>metacarpophalangeal (MCP) joint</td>
  </tr>
  <tr>
    <td>PIP_LONG_TOE_LEFT</td>
    <td>21</td>
    <td>proximal interphalangeal (PIP) joint</td>
  </tr>
  <tr>
    <td>DIP_LONG_TOE_LEFT</td>
    <td>22</td>
    <td>distal interphalangeal (DIP) joint</td>
  </tr>
  <tr>
    <td>BTIP_LONG_TOE_LEFT</td>
    <td>23</td>
    <td>bone tip (BTIP)</td>
  </tr>
  <tr>
    <td colspan="3">left big toe</td>
  </tr>
  <tr>
    <td>TMT_BIG_TOE_LEFT</td>
    <td>24</td>
    <td>tarsometatarsal (TMT) joint</td>
  </tr>
  <tr>
    <td>MTP_BIG_TOE_LEFT</td>
    <td>25</td>
    <td>metatarsophalangeal (MTP) joint</td>
  </tr>
  <tr>
    <td>IP_BIG_TOE_LEFT</td>
    <td>26</td>
    <td>interphalangeal (IP) joint</td>
  </tr>
  <tr>
    <td>BTIP_BIG_TOE_LEFT</td>
    <td>27</td>
    <td>bone tip (BTIP)</td>
  </tr>
</table>

## Torso
![Joints of the torso](https://github.com/scopus777/corpus/blob/master/img/torso_joints.png "Joints of the torso")

The image (Source: Modified version of [DIAGRAM PICTURE]) visualizes the joints of the torso used in the framework. The following table maps the joint to the corresponding `JointType` value. The wrists and ankles don't belong the torso in the context of the framework, but to visualize the transition points of the body parts they are shown again. 

<table>
  <tr>
    <th>JointType</th>
    <th>Number</th>
    <th>Joint</th>
  </tr>
  <tr>
    <td>SPINE_BASE</td>
    <td>1</td>
    <td>center hip</td>
  </tr>
  <tr>
    <td>SPINE_MID</td>
    <td>2</td>
    <td>middle of the spine</td>
  </tr>
  <tr>
    <td>SPINE_SHOULDER</td>
    <td>3</td>
    <td>spine and shoulder concourse</td>
  </tr>
  <tr>
    <td>NECK</td>
    <td>4</td>
    <td>center neck</td>
  </tr>
  <tr>
    <td>HEAD</td>
    <td>5</td>
    <td>head</td>
  </tr>
  <tr>
    <td>SHOULDER_RIGHT</td>
    <td>6</td>
    <td>right shoulder</td>
  </tr>
  <tr>
    <td>ELBOW_RIGHT</td>
    <td>7</td>
    <td>right elbow</td>
  </tr>
  <tr>
    <td>WRIST_RIGHT</td>
    <td>8</td>
    <td>right wrist</td>
  </tr>
  <tr>
    <td>SHOULDER_LEFT</td>
    <td>9</td>
    <td>left shoulder</td>
  </tr>
  <tr>
    <td>ELBOW_LEFT</td>
    <td>10</td>
    <td>left elbow</td>
  </tr>
  <tr>
    <td>WRIST_LEFT</td>
    <td>11</td>
    <td>left wrist</td>
  </tr>
  <tr>
    <td>HIP_RIGHT</td>
    <td>12</td>
    <td>right hip</td>
  </tr>
  <tr>
    <td>KNEE_RIGHT</td>
    <td>13</td>
    <td>right knee</td>
  </tr>
  <tr>
    <td>ANKLE_RIGHT</td>
    <td>14</td>
    <td>right ankle</td>
  </tr>
  <tr>
    <td>HIP_LEFT</td>
    <td>15</td>
    <td>left hip</td>
  </tr>
  <tr>
    <td>KNEE_LEFT</td>
    <td>16</td>
    <td>left knee</td>
  </tr>
  <tr>
    <td>ANKLE_LEFT</td>
    <td>17</td>
    <td>left ankle</td>
  </tr>
</table>

## Acknowledgment
There is a sensor-fusion library called [Jester] with similar goals like the here provided framework. Some good ideas where adopted to this framework.

[Grizzly Project]:https://grizzly.java.net
[Jersey]:https://jersey.java.net
[Kinect-for-Java]:https://github.com/ccgimperial/Kinect-for-Java
[jovr]:https://github.com/jherico/jovr
[myo-java]:https://github.com/NicholasAStuart/myo-java
[Maven]:http://maven.apache.org
[LadyofHats]:http://commons.wikimedia.org/wiki/Hand#mediaviewer/File:Scheme_human_hand_bones-numbers.svg
[Tasha]:http://de.wikipedia.org/wiki/Fu%C3%9F#mediaviewer/File:Ospied-de.svg
[DIAGRAM PICTURE]:http://diagrampic.com/?attachment_id=55
[Jester]:https://github.com/kevinschapansky/Jester