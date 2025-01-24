import {render, screen} from "@testing-library/react";
import {SERVERS_TABLE_CONSTANTS} from "@/constants/servers-table-constants";
import GpuDetails from "@/components/servers/gpu-details/GpuDetails";
import {GpuDetailsEmpty, GpuDetailsNormal} from "@/components/servers/gpu-details/GpuDetails.stories";

describe('GpuDetails', () => {
    it('Should render when empty', () => {
        render(<GpuDetails {...GpuDetailsEmpty.args}/>)
        expect(screen.getByText('Not available', {})).toBeVisible()
    })
    it('Should render with gpu', () => {
        render(<GpuDetails {...GpuDetailsNormal.args}/>)
        expect(screen.getByText(SERVERS_TABLE_CONSTANTS.GPU_VENDOR, {})).toBeVisible();
        expect(screen.getByText(SERVERS_TABLE_CONSTANTS.GPU_MODEL_NAME, {})).toBeVisible();
        GpuDetailsNormal.args.gpus.forEach(gpu => {
            expect(screen.getByRole("row", {name: gpu.Vendor + " " + gpu.ModelName})).toBeVisible();
        })
    })
})
