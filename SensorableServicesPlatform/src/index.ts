import { startInformCustomAdls } from "../packages/inform-custom-adls/src"
import { startInformGenericAdls } from "../packages/inform-generic-adls/src"
import { startSensorsBackUpService } from "../packages/sensors-back-up/src"
import { startInformActivitiesToRegister } from "../packages/inform-activities-to-register/src"
import { startActivitiesBackUpService } from "../packages/activities-back-up/src"

startInformCustomAdls()
startInformGenericAdls()
startSensorsBackUpService()
startInformActivitiesToRegister()
startActivitiesBackUpService()
